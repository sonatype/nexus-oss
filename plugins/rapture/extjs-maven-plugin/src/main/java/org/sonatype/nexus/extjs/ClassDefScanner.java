/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.extjs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.codehaus.plexus.util.Scanner;
import org.codehaus.plexus.util.dag.CycleDetectedException;
import org.codehaus.plexus.util.dag.DAG;
import org.codehaus.plexus.util.dag.TopologicalSorter;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.ArrayLiteral;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.ObjectLiteral;
import org.mozilla.javascript.ast.ObjectProperty;
import org.mozilla.javascript.ast.PropertyGet;
import org.mozilla.javascript.ast.StringLiteral;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.mozilla.javascript.Token.CALL;

/**
 * ExtJS 4+ class definition scanner.
 *
 * @since 3.0
 */
public class ClassDefScanner
{
  private final CompilerEnvirons env = new CompilerEnvirons();

  private final Map<String,ClassDef> classes = Maps.newHashMap();

  /**
   * Scan a set of files and return a list of class definitions in dependency order.
   * ie. Foo extends Bar, Bar will be before Foo.
   */
  public List<ClassDef> scan(final Scanner files) throws IOException, CycleDetectedException {
    checkNotNull(files);

    // reset and scan for files
    classes.clear();
    files.scan();

    // scan each file
    String[] included = files.getIncludedFiles();
    for (String path : included) {
      File file = new File(files.getBasedir(), path);
      scan(file);
    }

    // build the graph
    DAG graph = new DAG();
    for (ClassDef def : classes.values()) {
      graph.addVertex(def.getName());
      for (String dep : def.getDependencies()) {
        graph.addEdge(def.getName(), dep);
      }
    }

    // result sorted list of class definitions
    List<ClassDef> results = Lists.newArrayList();
    for (String className : TopologicalSorter.sort(graph)) {
      ClassDef def = classes.get(className);
      if (def != null) {
        results.add(def);
      }
    }

    return results;
  }

  /**
   * Scan the given file for class definitions and accumulate dependencies.
   */
  private void scan(final File source) throws IOException {
    Parser parser = new Parser(env);
    Reader reader = new BufferedReader(new FileReader(source));
    try {
      AstRoot root = parser.parse(reader, source.getAbsolutePath(), 0);
      root.visit(new DependencyAccumulator(source));
    }
    finally {
      reader.close();
    }
  }

  private class DependencyAccumulator
    implements NodeVisitor
  {
    private final File source;

    private ClassDef current;

    private DependencyAccumulator(final File source) {
      this.source = source;
    }

    @Override
    public boolean visit(final AstNode node) {
      int type = node.getType();

      switch (type) {
        case CALL:
          FunctionCall call = (FunctionCall) node;
          String name = nameOf(call.getTarget());
          if (name.equals("Ext.define")) {
            return processClassDef(call);
          }
          break;
      }

      return true; // process children
    }

    /**
     * Find the textual name of the given node.
     */
    private String nameOf(final AstNode node) {
      if (node instanceof Name) {
        return ((Name) node).getIdentifier();
      }
      else if (node instanceof PropertyGet) {
        PropertyGet prop = (PropertyGet) node;
        return String.format("%s.%s", nameOf(prop.getTarget()), nameOf(prop.getProperty()));
      }
      else if (node instanceof StringLiteral) {
        return ((StringLiteral) node).getValue();
      }
      return null;
    }

    /**
     * Process an {@code Ext.define} class definition.
     */
    private boolean processClassDef(final FunctionCall node) {
      List<AstNode> args = node.getArguments();
      checkState(args.size() >= 2); // simple def or def with callback

      // class-name
      checkState(args.get(0) instanceof StringLiteral);
      String className = nameOf(args.get(0));

      current = new ClassDef(className, source);
      classes.put(className, current);

      // class def object
      checkState(args.get(1) instanceof ObjectLiteral);
      ObjectLiteral obj = (ObjectLiteral) args.get(1);
      for (ObjectProperty prop : obj.getElements()) {
        String name = nameOf(prop.getLeft());
        switch (name) {
          case "extend":
            // string literal only
            dependencyStringLiteral(prop.getRight());
            break;

          case "override":
            // string literal only
            dependencyStringLiteral(prop.getRight());
            break;

          case "requires":
            // array of string literals only
            dependenciesArrayStringLiteral(prop.getRight());
            break;

          case "uses":
            // array of string literals only
            dependenciesArrayStringLiteral(prop.getRight());
            break;

          case "mixins": {
            // array of strings, or object
            if (prop.getRight() instanceof ArrayLiteral) {
              dependenciesArrayStringLiteral(prop.getRight());
            }
            else if (prop.getRight() instanceof ObjectLiteral) {
              ObjectLiteral child = (ObjectLiteral) prop.getRight();
              for (ObjectProperty element : child.getElements()) {
                dependencyStringLiteral(element.getRight());
              }
            }
            else {
              throw new IllegalStateException();
            }
            break;
          }

          // TODO: process MVC properties? though we have to work harder to resolve the real class-names
          // TODO: controllers: models, stores, views
          // TODO: stores: model
          // TODO: models: ???
        }
      }

      return false; // don't process children
    }

    /**
     * Add a dependency on the current class-def for the given string literal node value.
     */
    private void dependencyStringLiteral(final AstNode node) {
      checkState(node instanceof StringLiteral);
      //noinspection ConstantConditions
      StringLiteral string = (StringLiteral)node;
      current.getDependencies().add(string.getValue());
    }

    /**
     * Add dependencies on the current class-def for the given array node of string literals.
     */
    private void dependenciesArrayStringLiteral(final AstNode node) {
      checkState(node instanceof ArrayLiteral);
      //noinspection ConstantConditions
      ArrayLiteral array = (ArrayLiteral) node;
      for (AstNode element : array.getElements()) {
        dependencyStringLiteral(element);
      }
    }
  }
}
