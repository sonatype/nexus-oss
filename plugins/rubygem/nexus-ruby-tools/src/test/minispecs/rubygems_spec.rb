require 'nexus/rubygems'
require 'minitest/spec'
require 'minitest/autorun'
require 'stringio'

describe Nexus::Rubygems do

  subject do
    r = Nexus::Rubygems.new
    def r.name_versions_map( source, modified, m = nil )
      @m = m if m
      @m
    end
    def r.name_preversions_map( source, modified, m = nil )
      @mm = m if m
      @mm
    end
    r
  end

  let( :a1java ) { [ 'a', '1', 'java' ] }
  let( :a2java ) { [ 'a', '2', 'java' ] }
  let( :a1 ) { ['a', '1', 'ruby' ] }
  let( :a2 ) { ['a', '2', 'ruby' ] }
  let( :b4 ) { ['b', '4', 'ruby' ] }

  let( :nothing ) do
    tmp = File.join( 'target', 'merge_nothing' )
    File.open( tmp, 'w' ){ |f| f.print Marshal.dump( [ a1java, a2, b4 ] ) }
    tmp
  end

  let( :something ) do
    tmp = File.join( 'target', 'merge_something' )
    File.open( tmp, 'w' ){ |f| f.print Marshal.dump( [ a2java, a2 ] ) }
    tmp
  end

  let( :broken_from ) do
    File.join( 'src', 'test', 'resources', 'broken' )
  end

  let( :broken_to ) do
    File.join( 'target', 'broken' )
  end

  before do
    FileUtils.rm_rf( broken_to )
    FileUtils.cp_r( broken_from, broken_to )
  end

  it 'should take the latest version' do
    specs = [ a2, b4, a1 ]
    subject.send( :regenerate_latest, specs ).must_equal [ a2, b4 ]
  end

  it 'should take the latest version per platform' do
    specs = [ a1java, a2java, a2, b4 ]
    subject.send( :regenerate_latest, specs ).sort.must_equal [ a2java, a2, b4 ]
  end

  it 'should take the latest version one per platform' do
    specs = [ a1java, a2, b4 ]
    subject.send( :regenerate_latest, specs ).must_equal [ a1java, a2, b4 ]
  end

  it 'should merge nothing' do
    dump = subject.send( :merge_specs, [ nothing ] ).pack 'C*'
    Marshal.load( StringIO.new( dump ) ).must_equal [ a1java, a2, b4 ]
  end

  it 'should merge something' do
    dump = subject.send( :merge_specs, [ nothing, something ] ).pack 'C*'
    Marshal.load( StringIO.new( dump ) ).must_equal [ a1java,  a2java, a2, b4 ]
  end

  it 'should merge something latest' do
    dump = subject.send( :merge_specs, [ nothing, something ], true ).pack 'C*'
    Marshal.load( StringIO.new( dump ) ).must_equal [ a2java, a2, b4 ]
  end

  it 'should merge dependencies' do
    a = [ {:name=>"jbundler", :number=>"0.5.5", :platform=>"ruby", :dependencies=>[["bundler", "~> 1.5"], ["ruby-maven", "< 3.1.2, >= 3.1.1.0.1"]]}, {:name=>"jbundler", :number=>"0.5.4", :platform=>"ruby", :dependencies=>[["bundler", "~> 1.2"], ["ruby-maven", "< 3.1.2, >= 3.1.1.0.1"]]}, {:name=>"jbundler", :number=>"0.5.3", :platform=>"ruby", :dependencies=>[["bundler", "~> 1.2"], ["ruby-maven", "< 3.1.1, >= 3.1.0.0.1"]]} ]

    aa = [ {:name=>"jbundler", :number=>"0.5.5", :platform=>"ruby", :dependencies=>[["bundler", "~> 1.5"]] }]

    b = [ {:name=>"bundler", :number=>"1.6.0.rc2", :platform=>"ruby", :dependencies=>[]}, {:name=>"bundler", :number=>"1.6.0.rc", :platform=>"ruby", :dependencies=>[]} ]
    
    dump = subject.merge_dependencies( false, java.io.ByteArrayInputStream.new( Marshal.dump( b ).to_java.bytes ),
                                       java.io.ByteArrayInputStream.new( Marshal.dump( a ).to_java.bytes ) ).pack 'C*'
    Marshal.load( StringIO.new( dump ) ).must_equal b + a

    dump = subject.merge_dependencies( true, java.io.ByteArrayInputStream.new( Marshal.dump( b ).to_java.bytes ),
                                       java.io.ByteArrayInputStream.new( Marshal.dump( a ).to_java.bytes ),
                                       java.io.ByteArrayInputStream.new( Marshal.dump( aa ).to_java.bytes ) ).pack 'C*'
    Marshal.load( StringIO.new( dump ) ).must_equal b + a

    map = subject.split_dependencies( java.io.ByteArrayInputStream.new( Marshal.dump( a + b ).to_java.bytes ) )
    Marshal.load( StringIO.new( map['jbundler'].pack( 'C*' ) ) ).must_equal a
    Marshal.load( StringIO.new( map['bundler'].pack( 'C*' ) ) ).must_equal b
  end

  it 'purge api files' do
    subject.purge_broken_depencency_files( broken_to )
    dirs = Dir[ File.join( broken_to, 'api', '**', '*' ) ]
    dirs.each do |f|
      if File.file?( f )
        f.must_match /.json.rz$/
      else
        File.directory?( f ).must_equal true
      end
    end
    dirs.size.must_equal 3
  end

  it 'purge gemspec files' do
    subject.purge_broken_gemspec_files( broken_to )
    dirs = Dir[ File.join( broken_to, 'quick', '**', '*' ) ]
    dirs.each do |f|
      File.directory?( f ).must_equal true
    end
    dirs.size.must_equal 2
  end

  it 'rebuild rubygems metadata' do
    subject.recreate_rubygems_index( broken_to )
    Dir[ File.join( broken_to, '*specs.4.8.gz' ) ].size.must_equal 3
    Dir[ File.join( broken_to, '*specs.4.8' ) ].size.must_equal 0
    Dir[ File.join( broken_to, '*' ) ].size.must_equal 6
    # this includes all the defaultgems from jruby
    # also includes all the gems coming from maven-tools dependency !!
    # i.e. a new jruby.version can change that number !!
    # puts Dir[ File.join( broken_to, 'quick', '**', '*' ) ].join("\n")
    Dir[ File.join( broken_to, 'quick', '**', '*' ) ].size.must_equal 31
  end

end
