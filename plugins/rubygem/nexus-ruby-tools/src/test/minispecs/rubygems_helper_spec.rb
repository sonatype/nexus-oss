require 'nexus/rubygems_helper'
require 'minitest/spec'
require 'minitest/autorun'
require 'stringio'

class RGH
  include Nexus::RubygemsHelper
end
describe Nexus::RubygemsHelper do

  subject { RGH.new }

  let( :a1java ) { [ 'a', '1', 'java' ] }
  let( :a2java ) { [ 'a', '2', 'java' ] }
  let( :a1 ) { ['a', '1', 'ruby' ] }
  let( :a2 ) { ['a', '2', 'ruby' ] }
  let( :b4 ) { ['b', '4', 'ruby' ] }

  it 'should take the latest version' do
    specs = [ a2, b4, a1 ]
    subject.regenerate_latest( specs ).must_equal [ a2, b4 ]
  end

  it 'should take the latest version per platform' do
    specs = [ a1java, a2java, a2, b4 ]
    subject.regenerate_latest( specs ).sort.must_equal [ a2java, a2, b4 ]
  end

  it 'should take the latest version one per platform' do
    specs = [ a1java, a1, a2, b4 ]
    subject.regenerate_latest( specs ).must_equal [ a1java, a2, b4 ]
  end
end
