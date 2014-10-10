require 'bundler'
require 'bundler/cli'
require 'stringio'
require 'thor/shell/basic'
class Thor::Shell::Basic
  
  def self.stdout
    @stdout ||= StringIO.new
  end

  def stdout
    self.class.stdout
  end
  
  def stderr
    @stderr ||= StringIO.new
  end
  
end

module Nexus

  class BundleRunner

    def exec( *args )

      ENV['PATH'] ||= '' # just make sure bundler has a PATH variable

      Bundler::CLI.start( args )

      Thor::Shell::Basic.stdout.string

    rescue SystemExit => e
      raise shell.stderr.string if e.exit_code != 0

      Thor::Shell::Basic.stdout.string

    rescue Exception => e
      puts Thor::Shell::Basic.stdout.string
      trace = e.backtrace.join("\n\t")
      raise "#{e.message}\n\t#{trace}"

    ensure
      Thor::Shell::Basic.stdout.reopen
    end

  end
end
# this makes it easy for a scripting container to 
# create an instance of this class
Nexus::BundleRunner
