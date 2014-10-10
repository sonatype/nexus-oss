# -*- mode: ruby -*-
# -*- encoding: utf-8 -*-

require './lib/nexus/version'

Gem::Specification.new do |s|
  s.name = 'nexus'
  s.version = Nexus::VERSION

  s.authors = ["Nick Quaranto", 'Christian Meier']
  s.email = ['nick@quaran.to', 'm.kristian@web.de']

  s.license = 'MIT-LICENSE'

  s.summary = 'Gem Command to interact with Nexus server'
  s.description = 'Adds a command to RubyGems for uploading gems to a nexus server.'


  s.executables = ['nbundle']
  s.files = ["MIT-LICENSE", "Rakefile"]
  s.files += Dir['lib/**/*.rb']
  s.files += Dir['test/**/*.rb']

  s.homepage = 'https://github.com/sonatype/nexus-ruby-support/tree/master/nexus-gem'
  s.post_install_message = %q{
========================================================================

           Thanks for installing Nexus gem! You can now run:

    gem nexus          publish your gems onto Nexus server

    nbundle            a bundler fork with mirror support. for bundler before 1.5.0
                       
add a mirror with:

    bundle config mirror.http://rubygems.org http://localhost:8081/nexus/content/repositories/rubygems.org

for bundler before 1.5.0 use 'nbundle' instead of 'bundle' to use the mirror

========================================================================

}
  s.require_paths = ["lib"]

  s.add_development_dependency('rake', '~> 10.1')
  s.add_development_dependency('ruby-maven', '~> 3.1.0.0.0')
  s.add_development_dependency('shoulda', "~> 3.1")
  s.add_development_dependency('rr', "~> 1.1")
  # to use a version which works
  s.add_development_dependency('activesupport', "~> 4.0.0")
  s.add_development_dependency('webmock', "~> 1.8", "< 1.16")
end
