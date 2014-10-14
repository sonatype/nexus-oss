require 'minitest/autorun'

require 'shoulda'

# for some reasons the refute_predicate is missing when executing
# via jruby-1.7.4
module ActiveSupport
  class TestCase < ::MiniTest::Unit::TestCase
    def refute_predicate
    end
  end
end

require 'active_support'
require 'active_support/test_case'
require 'webmock'
require 'rr'

begin
  require 'redgreen'
rescue LoadError
end

WebMock.disable_net_connect!

$:.unshift File.expand_path(File.join(File.dirname(__FILE__), ".."))

require "rubygems_plugin"

class CommandTest < ActiveSupport::TestCase
  include WebMock::API
  include ShouldaContextLoadable 

  def teardown
    WebMock.reset!
  end
end

def stub_config(config)
  file = Gem::ConfigFile.new({})
  config.each { |key, value| file[key] = value }
  stub(Gem).configuration { config }
end

def assert_said(command, what)
  assert_received(command) do |command|
    command.say(what)
  end
end

def assert_never_said(command, what)
  assert_received(command) do |command|
    command.say(what).never
  end
end
