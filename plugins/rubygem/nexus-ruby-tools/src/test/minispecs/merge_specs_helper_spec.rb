require 'nexus/merge_specs_helper_impl'
require 'minitest/spec'
require 'minitest/autorun'
require 'stringio'

describe Nexus::MergeSpecsHelperImpl do

  # just create it the same as the java app would do it
  subject { Nexus::MergeSpecsHelperImpl.new }

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
    File.open( tmp, 'w' ){ |f| f.print Marshal.dump( [ a2java, a2, a1 ] ) }
    tmp
  end

  it 'should merge nothing' do
    subject.add( nothing )
    subject.marshal_load( subject.input_stream( false ) ).must_equal [ a1java, a2, b4 ]
  end

  it 'should merge something' do
    subject.add( nothing )
    subject.add( something )
    subject.marshal_load( subject.input_stream( false ) ).must_equal [ a1java, a1, a2java, a2, b4 ]
  end

  it 'should merge something latest' do
    subject.add( nothing )
    subject.add( something )
    subject.marshal_load( subject.input_stream( true ) ).must_equal [ a2java, a2, b4 ]
  end
end
