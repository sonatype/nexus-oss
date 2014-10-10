#-*- mode: ruby -*-

require 'rake'
require 'rake/testtask'
require 'rubygems/package_task'

task :default => [ :test, :package ]

Gem::PackageTask.new( Gem::Specification.load( 'nexus.gemspec' ) ) do
end

Rake::TestTask.new(:test) do |t|
  t.libs << "test"
  t.test_files = FileList['test/*_test.rb']
  t.verbose = true
end

# vim: syntax=Ruby
