require 'command_helper'

class Gem::Commands::FakeCommand < Gem::AbstractCommand
  def description
    'fake command'
  end

  def initialize
    super 'fake', description
  end

  def execute
  end
end

class AbstractCommandTest < CommandTest

  context "with an fake command" do
    setup do
      @command = Gem::Commands::FakeCommand.new
      Gem.configuration.verbose = false
      stub(@command).say
      ENV['http_proxy'] = nil
      ENV['HTTP_PROXY'] = nil
    end

    context "parsing the proxy" do
      should "return nil if no proxy is set" do
        stub_config(:http_proxy => nil)
        assert_equal nil, @command.http_proxy( nil )
      end

      should "return nil if the proxy is set to :no_proxy" do
        stub_config(:http_proxy => :no_proxy)
        assert_equal nil, @command.http_proxy( 'asd' )
      end

      should "return a proxy as a URI if set" do
        stub_config( :http_proxy => 'http://proxy.example.org:9192' )
        assert_equal 'proxy.example.org', @command.http_proxy( 'http://asd' ).host
        assert_equal 9192, @command.http_proxy( 'http://asd' ).port
      end

      should "return a proxy as a URI if set by environment variable" do
        ENV['http_proxy'] = "http://jack:duck@192.168.1.100:9092"
        assert_equal "192.168.1.100", @command.http_proxy( 'http://asd' ).host
        assert_equal 9092, @command.http_proxy( 'http://asd' ).port
        assert_equal "jack", @command.http_proxy( 'http://asd' ).user
        assert_equal "duck", @command.http_proxy( 'http://asd' ).password
      end
    end

    should "sign in if no authorization and no nexus url in config" do
      config_path = File.join( 'pkg', 'configsomething')
      FileUtils.rm_f( config_path )
      @command.options[ :nexus_config ] = config_path
      stub(@command).sign_in
      stub(@command).configure_url
      @command.setup
      assert_received(@command) { |command| command.configure_url }
      assert_received(@command) { |command| command.sign_in }
    end

    should "sign in if --clear-config is set" do
      config_path = File.join( 'pkg', 'config_clear')
      FileUtils.rm_f( config_path )
      @command.options[ :nexus_config ] = config_path
      stub(@command).sign_in
      stub(@command).configure_url
      stub(@command).options do
        { :nexus_clear => true,
          :nexus_config => config_path
        }
      end
      @command.setup
      assert_received(@command) { |command| command.sign_in }
      assert_received(@command) { |command| command.configure_url }
    end

    should "sign in if --password is set" do
      config_path = File.join( 'pkg', 'config_password')
      File.open( config_path, 'w') do |f|
        h = { :url => 'http://example.com' }
        f.write h.to_yaml
      end
      @command.options[ :nexus_config ] = config_path
      @command.options[ :nexus_prompt ] = true
      stub(@command).sign_in
      @command.setup
      assert_received(@command) { |command| command.sign_in }
    end


    should "always return stored authorization and url" do
      config_path = File.join( 'pkg', 'configsomething')
      FileUtils.rm_f( config_path )
      @command.options[ :nexus_config ] = config_path
      @command.options[ :nexus_prompt ] = true
      @command.config.url = 'something'
      @command.config.authorization = 'something'
      assert_not_nil @command.authorization
      assert_not_nil @command.url
    end

    should "not sign in nor configure if authorizaton and url exists" do
      config_path = File.join( 'pkg', 'configsomething')
      FileUtils.rm_f( config_path )
      @command.options[ :nexus_config ] = config_path
      stub(@command).authorization { "1234567890" }
      stub(@command).url { "abc" }
      stub(@command).sign_in
      stub(@command).configure_url
      @command.setup
      assert_received(@command) { |command| command.configure_url.never }
      assert_received(@command) { |command| command.sign_in.never }
    end

    context "using the proxy" do
      setup do
        stub_config( :http_proxy => "http://gilbert:sekret@proxy.example.org:8081" )
        @proxy_class = Object.new
        mock(Net::HTTP).Proxy('proxy.example.org', 8081, 'gilbert', 'sekret') { @proxy_class }
        @command.use_proxy!( 'http://asd' )
      end

      should "replace Net::HTTP with a proxy version" do
        assert_equal @proxy_class, @command.proxy_class
      end
    end

    context 'separeted config per repo key' do
      should 'store the config on per key' do
        config_path = File.join( 'pkg', 'configrepo')
        FileUtils.rm_f( config_path )
        @command.options[ :nexus_config ] = config_path
        @command.options[ :nexus_repo ] = :first
        @command.config.url = :thing
        @command.options[ :nexus_repo ] = :second
        @command.send :instance_variable_set, '@config'.to_sym, nil
        @command.config.url = :otherthing
        @command.options[ :nexus_repo ] = nil
        @command.send :instance_variable_set, '@config'.to_sym, nil
        @command.config.url = :nothing
        assert_equal( Gem.configuration.load_file(config_path),
                      { :first => {:url => :thing}, 
                        :second => {:url => :otherthing},
                        :url => :nothing } )
      end
    end

    context "clear username + password" do

      should "clear stored authorization" do
        stub(@command).options { {:nexus_config => File.join( 'pkg', 
                                                              'config') } }
        stub(@command).say
        stub(@command).ask { nil }
        stub(@command).ask_for_password { nil }
        @command.config.authorization = 'some authentication'

        @command.sign_in
        assert_nil @command.authorization
      end
    end

    context "encryption" do

    end

    context "signing in" do
      setup do
        @username = "username"
        @password = "password 01234567890123456789012345678901234567890123456789"
        @key = "key"

        stub(@command).say
        stub(@command).ask { @username }
        stub(@command).ask_for_password { @password }
        stub(@command).options { {:nexus_config => File.join( 'pkg', 
                                                              'configsign') } }
        @command.config.authorization = @key
      end
      
      should "ask for username and password" do
        @command.sign_in
        assert_received(@command) { |command| command.ask("Username: ") }
        assert_received(@command) { |command| command.ask_for_password("Password: ") }
        assert_equal( @command.config.authorization, 
                      "Basic dXNlcm5hbWU6cGFzc3dvcmQgMDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODk=" )
      end

      should "say that we signed in" do
        @command.sign_in
        assert_received(@command) { |command| command.say("Enter your Nexus credentials") }
        assert_received(@command) { |command| command.say("Your Nexus credentials has been stored in pkg/configsign") }
        assert_equal( @command.config.authorization, 
                      "Basic dXNlcm5hbWU6cGFzc3dvcmQgMDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODk=" )
      end
    end

    context "configure nexus url" do
      setup do
        @url = "http://url"
 
        stub(@command).say
        stub(@command).ask { @url }
        stub(@command).options { {:nexus_config => File.join( 'pkg', 
                                                              'configurl') } }
        @command.config.url = @url
      end

      should "ask for nexus url" do
        @command.configure_url
        assert_received(@command) { |command| command.ask("URL: ") }
        assert_equal( @command.config.url, "http://url" )
      end

      should "say that we configured the url" do
        @command.configure_url
        assert_received(@command) { |command| command.say("Enter the URL of the rubygems repository on a Nexus server") }
        assert_received(@command) { |command| command.say("The Nexus URL has been stored in ~/.gem/nexus") }
        assert_equal( @command.config.url, "http://url" )
      end
    end
  end
end
