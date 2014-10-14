require 'command_helper'
require 'fileutils'

class NexusCommandTest < CommandTest

  context "all repos" do
    setup do
      @command = Gem::Commands::NexusCommand.new
      config = File.join( 'pkg', 'allrepocfg' )
      FileUtils.rm_f( config )
      @command.options[ :nexus_config ] = config
    end

    should "list" do
      @command.options[ :nexus_all_repos ] = true
      mock(@command).list_repos
      @command.execute
      assert_received(@command) { |command| command.list_repos }
    end

    should "not list" do
      @command.options[ :nexus_all_repos ] = false
      mock(@command).configure_url
      mock(@command).sign_in
      mock(@command).send_gem
      @command.execute
      assert_received(@command) { |command| command.configure_url }
      assert_received(@command) { |command| command.sign_in }
      assert_received(@command) { |command| command.send_gem }
    end
  end

  context "encryption" do
    setup do
      @command = Gem::Commands::NexusCommand.new
      @with = File.join( 'pkg', 'encryption' )
      @from = File.join( 'test', 'encryption' )
      [ ".config", ".secrets" ].each do |postfix|
        FileUtils.cp( @from + postfix, @with + postfix )
      end

      FileUtils.cp( @from, @with )
    end

    should "with secrets file" do
      @command.options[ :nexus_config ] = @with + ".config"
      @command.options[ :nexus_encrypt ] = true
      mock(@command).ask( "Enter your Nexus encryption credentials (no prompt)" ) { 'behappy' }
      @command.execute
      assert_equal( File.read( @with + ".config" ), 
                    File.read( @from + ".config" ) )
      assert_equal( File.read( @with + ".secrets" ) != File.read( @from + ".secrets" ), true )

      @command.options.delete( :nexus_encrypt )
      mock(@command).prompt_encryption
      mock(@command).send_gem
      @command.execute
      assert_equal @command.authorization != /^Basic /, true
      assert_received(@command) { |command| command.prompt_encryption }
      assert_received(@command) { |command| command.send_gem }
    end

    should "without secrets file" do
      @command.options[ :nexus_config ] = @with
      @command.options[ :nexus_encrypt ] = true
      mock(@command).ask( "Enter your Nexus encryption credentials (no prompt)" ) { 'behappy' }
      @command.execute
      assert_equal( File.read( @with ) != File.read( @from ), true )

      @command.options.delete( :nexus_encrypt )
      mock(@command).prompt_encryption
      mock(@command).send_gem
      @command.execute
      assert_equal @command.authorization != /^Basic /, true
      assert_received(@command) { |command| command.prompt_encryption }
      assert_received(@command) { |command| command.send_gem }
    end

    should "prompt when configured" do
      config_path = File.join( 'pkg', 'encconfig_prompt')
      File.open( config_path, 'w') do |f|
        h = { :url => 'http://example.com',
          :authorization => 'something', 
          :token => 'pe/ty1I5sl7qD5drdsSXOBi0peRYOqdhhvRXJBjzzNY=' }
        f.write h.to_yaml
      end
      stub(@command).options { {:nexus_config => config_path } }
      stub(@command).ask_for_password { "behappy" }
      
      @command.setup
      assert_equal @command.config.encrypted?, true
    end
  end

  context "decryption" do
    setup do
      @command = Gem::Commands::NexusCommand.new
      @with = File.join( 'pkg', 'decryption' )
      @from = File.join( 'test', 'decryption' )
      [ ".config", ".secrets" ].each do |postfix|
        FileUtils.cp( @from + postfix, @with + postfix )
      end

      FileUtils.cp( @from, @with )
    end

    should "with secrets file" do
      @command.options[ :nexus_config ] = @with + ".config"
      @command.options[ :nexus_encrypt ] = false
      mock(@command).ask( "Enter your Nexus encryption credentials (no prompt)" ) { 'behappy' }
      @command.execute
      assert_equal( File.read( @with + ".config" ), 
                    File.read( @from + ".config" ) )
      assert_equal( File.read( @with + ".secrets" ) != File.read( @from + ".secrets" ), true )

      @command.options.delete( :nexus_encrypt )
      mock(@command).send_gem
      @command.execute
      assert_equal @command.authorization != /^Basic /, true
      assert_received(@command) { |command| command.send_gem }
    end

    should "without secrets file" do
      @command.options[ :nexus_config ] = @with
      @command.options[ :nexus_encrypt ] = false
      mock(@command).ask( "Enter your Nexus encryption credentials (no prompt)" ) { 'behappy' }
      @command.execute
      assert_equal( File.read( @with ) != File.read( @from ), true )

      @command.options.delete( :nexus_encrypt )
      mock(@command).send_gem
      @command.execute
      assert_equal @command.authorization != /^Basic /, true
      assert_received(@command) { |command| command.send_gem }
    end
  end

  context "clear credentials" do
    setup do
      @command = Gem::Commands::NexusCommand.new
      @with = File.join( 'pkg', 'clearcredentials' )
      from = File.join( 'test', 'clearcredentials' )
      [ ".config", ".secrets" ].each do |postfix|
        FileUtils.cp( from + postfix, @with + postfix )
      end

      FileUtils.cp( from, @with )
    end

    should "with secrets file" do
      @command.options[ :nexus_config ] = @with + ".config"
      @command.options[ :nexus_clear_all ] = true
      mock(@command).ask( 'delete all current credentials ? (y/N)' ){ 'y' }
      @command.execute
      assert_equal( File.exists?( @with + ".config" ), true )
      assert_equal( File.exists?( @with + ".secrets" ), false )

      @command.options.delete( :nexus_clear_all )
      mock(@command).sign_in
      mock(@command).send_gem
      @command.execute
      assert_received(@command) { |command| command.sign_in }
      assert_received(@command) { |command| command.send_gem }
    end

    should "without secrets file" do
      @command.options[ :nexus_config ] = @with
      @command.options[ :nexus_clear_all ] = true
      mock(@command).ask( 'delete all current credentials ? (y/N)' ){ 'y' }
      @command.execute

      @command.options.delete( :nexus_clear_all )
      mock(@command).sign_in
      mock(@command).send_gem
      @command.execute
      assert_received(@command) { |command| command.sign_in }
      assert_received(@command) { |command| command.send_gem }
    end
  end

  context "secrets file" do
    setup do
      @command = Gem::Commands::NexusCommand.new
      @with = File.join( 'pkg', 'secretsfile' )
      from = File.join( 'test', 'secretsfile' )
      [ ".config", ".secrets" ].each do |postfix|
        FileUtils.cp( from + postfix, @with + postfix )
      end

      FileUtils.cp( from, @with )
    end

    should "delete secrets file" do
      @command.options[ :nexus_config ] = @with + ".config"
      @command.options[ :nexus_secrets ] = false
      @command.execute
      assert_equal( File.exists?( @with + ".secrets" ), false )

      @command.options.delete( :nexus_secrets )
      mock(@command).send_gem
      @command.execute
      assert_received(@command) { |command| command.send_gem }
    end

    should "create secrets file" do
      @command.options[ :nexus_config ] = @with
      @command.options[ :nexus_secrets ] = @with + '.newsecrets'
      @command.execute
      assert_equal( File.exists?( @with + ".newsecrets" ), true )

      @command.options.delete( :nexus_secrets )
      mock(@command).send_gem
      @command.execute
      assert_received(@command) { |command| command.send_gem }
    end

    should "move secrets file" do
      @command.options[ :nexus_config ] = @with + ".config"
      @command.options[ :nexus_secrets ] = @with + '.newsecrets'
      @command.execute
      assert_equal( File.exists?( @with + ".secrets" ), false )
      assert_equal( File.exists?( @with + ".newsecrets" ), true )

      @command.options.delete( :nexus_secrets )
      mock(@command).send_gem
      @command.execute
      assert_received(@command) { |command| command.send_gem }
    end
  end

  context "always prompt" do
    setup do
      @command = Gem::Commands::NexusCommand.new
      config = File.join( 'pkg', 'alwayspromptconfig' )
      FileUtils.cp( File.join( 'test', 'alwayspromptconfig' ),
                    config )
      @command.options[ :nexus_config ] = config
    end

    should "prompt" do
      mock(@command).sign_in
      mock(@command).send_gem
      @command.execute
      assert_received(@command) { |command| command.sign_in }
      assert_received(@command) { |command| command.send_gem }
    end

    should "prompt with repo" do
      @command.options[ :repo ] = 'hosted'
      mock(@command).sign_in
      mock(@command).send_gem
      @command.execute
      assert_received(@command) { |command| command.sign_in }
      assert_received(@command) { |command| command.send_gem }
    end

    should "not prompt" do
      @command.options[ :nexus_prompt_all ] = false
      mock(@command).say "setup nexus to store username/passwords"
      @command.execute
      @command.options.delete( :nexus_prompt_all )
      @command.config.authorization = 'asd'
      mock(@command).send_gem
      @command.execute
      assert_received(@command) { |command| command.send_gem }
    end
  end

  context "pushing" do
    setup do
      @command = Gem::Commands::NexusCommand.new
      stub(@command).say
    end

    should "setup and send the gem" do
      mock(@command).setup
      mock(@command).send_gem
      @command.execute
      assert_received(@command) { |command| command.setup }
      assert_received(@command) { |command| command.send_gem }
    end

    should "raise an error with no arguments" do
      assert_raise Gem::CommandLineError do
        @command.send_gem
      end
    end

    context "pushing a gem" do
      setup do

        @gem_path = "path/to/foo-0.0.0.gem"
        baseurl = 'http://localhost:8081/nexus/content/repositories/localgems'
        @url = baseurl + @gem_path.sub(/.*\//, '/gems/')
        @gem_binary = StringIO.new("gem")

        stub(@command).say
        stub(@command).options { {:args => [@gem_path]} }
        stub(Gem).read_binary(@gem_path) { @gem_binary }
        stub(@command).config do
          obj = Object.new
          def obj.authorization; "key"; end
          def obj.url; 'http://localhost:8081/nexus/content/repositories/localgems'; end
          obj
        end
        stub_request(:put, @url).to_return(:status => 201)
        
        @command.send_gem
      end

      should "say push was successful" do
        assert_received(@command) { |command| command.say("Uploading gem to Nexus...") }
        # due to webmock there is no status message
        assert_received(@command) { |command| command.say("") }
      end

      should "put to api" do
        # webmock doesn't pass body params on correctly :[
        assert_requested(:put, @url,
                         :times => 1)
        assert_requested(:put, @url,
                         :body => @gem_binary,
                         :headers => {
                           'Authorization' => 'key', 
                           'Content-Type' => 'application/octet-stream'
                         })
      end
    end
  end
end
