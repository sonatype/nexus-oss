require 'minitest/autorun'
require 'shoulda'
require 'fileutils'
require 'nexus/config'

class ConfigTest < ::MiniTest::Unit::TestCase
  include ShouldaContextLoadable 
  
  context 'storing url and authorization' do
    
    should 'with plain text file' do
      file = File.join( 'pkg', 'plainconfig' )
      FileUtils.rm_f file

      repos = [ nil, 'first', 'second' ]

      repos.each do |repo|
        c = Nexus::Config.new( file, repo )
        c.url = "http://example.com/#{repo}"
        c.authorization = "BASIC asddsa#{repo}" 

        assert_equal c.authorization, "BASIC asddsa#{repo}"
        assert_equal c.url, "http://example.com/#{repo}"
      end

      repos.each do |repo|
        c = Nexus::Config.new( file, repo )
        assert_equal c.authorization, "BASIC asddsa#{repo}"
        assert_equal c.url, "http://example.com/#{repo}"
      end

      assert_equal( Nexus::Config.new( file ).repos,
                    { "first"=>"http://example.com/first", 
                      "second"=>"http://example.com/second", 
                      "DEFAULT"=>"http://example.com/"} )
    end
    
  end

  context 'auxilary functions' do

    should 'encrypt and decrypt credentials' do
      file = File.join( 'pkg', 'auxconfig' )
      FileUtils.rm_f file

      [ nil, 'key' ].each do |repo|
        c = Nexus::Config.new( file, repo )
        c.authorization = 'BASIC asddsa'
        
        assert_equal c.authorization, 'BASIC asddsa'
        
        cc = Nexus::Config.new( file, repo )
        cc.password = 'be happy'
        cc.encrypt_credentials
        
        assert_equal cc.authorization, 'BASIC asddsa'
        
        ccc = Nexus::Config.new( file, repo )
        ccc.password = 'be happy'
        assert_equal ccc.authorization, 'BASIC asddsa'
        assert_equal ccc.send( :[], :iv ), cc.send( :[], :iv )
        
        cc.decrypt_credentials
        
        assert_equal cc.authorization, 'BASIC asddsa'
        
        ccc = Nexus::Config.new( file, repo )
        
        assert_equal ccc.authorization , 'BASIC asddsa'
        assert_equal ccc.send( :[], :iv ), nil
        assert_equal ccc.send( :[], :token ), nil
      end
    end
    
    should 'move credentials to secrets file' do
      file = File.join( 'pkg', 'auxcfg' )
      sfile = File.join( 'pkg', 'auxsrt' )
      FileUtils.rm_f file
      FileUtils.rm_f sfile
      
      repos = [ nil, 'first', 'second' ]

      repos.each do |repo|
        c = Nexus::Config.new( file, repo )
        c.url = "http://example.com/#{repo}"
        c.authorization = "BASIC asddsa#{repo}"
      end

      Nexus::Config.new( file ).new_secrets( sfile )
      assert File.exists?( sfile ), true
        
      repos.each do |repo|
        c = Nexus::Config.new( sfile, repo )
        assert_equal c.url, nil
        assert_equal c.authorization, "BASIC asddsa#{repo}"
      end

      c = Nexus::ConfigFile.new( file )
      assert_equal c[ :authorization, nil ], nil
      assert_equal c[ :secrets, nil ], sfile
        
      repos.each do |repo|
        c = Nexus::ConfigFile.new( file )
        assert_equal c[ :url, repo ], "http://example.com/#{repo}"
      end

      Nexus::Config.new( file ).new_secrets( nil )
      assert_equal File.exists?( sfile ), false

      repos.each do |repo|
        c = Nexus::Config.new( file, repo )
        assert_equal c.url, "http://example.com/#{repo}"
        assert_equal c.authorization, "BASIC asddsa#{repo}"
      end
    end
  end
end
