# test, build, deploy, push #

there a two ways of doing things. both result in **identical** gems !!

maven runs the test for some jruby versions in both 1.8 and 1.9 mode. i.e.
it is a build CI of jruby.

## the ruby way ##

prepare the dependencies

    bundle install
	
run tests

    bundle exec rake 
	
build and push gem to rubygems.org

    gem build nexus.gemspec
    gem push nexus-*.gem

install the gem into your local rubgems repository

    gem install -l nexus-*.gem
	
## the maven way ##

    mvn -Ppush
	
this will run the tests, package the gem and pushed it to rubygems.org

the test run with some jruby versions in both 1.8 as well 1.9 mode.

    mvn install

will install the packed gem artifact into the local repository.

install the gem into your local rubgems repository

    gem install -l target/nexus-*.gem
