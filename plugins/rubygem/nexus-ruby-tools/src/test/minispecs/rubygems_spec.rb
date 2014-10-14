require 'nexus/rubygems'
require 'minitest/spec'
require 'minitest/autorun'
require 'stringio'

describe Nexus::Rubygems do

  subject { Nexus::Rubygems.new }

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
