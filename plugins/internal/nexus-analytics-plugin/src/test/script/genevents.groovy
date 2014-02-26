/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

def workers = 10
def pool = Executors.newFixedThreadPool(workers)
def url = new URL('http://localhost:8081/nexus/service/local/status')
def times = 100_000
def tick = 1000
def counter = new AtomicInteger()

def task = {
  for (int i in 1..times) {
    url.text
    def count = counter.incrementAndGet()
    if (count % tick == 0) {
      print '.'
    }
  }
}

def start = new Date()

(1..workers).each {
  pool.submit(task)
  println "queued task: #$it"
}

println 'waiting for tasks'
pool.shutdown()
pool.awaitTermination(1, TimeUnit.HOURS)

println ''
println 'tasks completed'

def stop = new Date()
def e = stop.time - start.time
println "executed task $counter times; elasped $e ms"
