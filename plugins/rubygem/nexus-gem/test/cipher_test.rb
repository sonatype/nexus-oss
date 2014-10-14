require 'minitest/autorun'
require 'shoulda'
require 'fileutils'
require 'nexus/cipher'

class ConfigTest < ::MiniTest::Unit::TestCase
  include ShouldaContextLoadable 
  
  context 'no token' do

    should 'create token' do
      c = Nexus::Cipher.new( 'behappy' )
      assert_equal( c.token.nil?, false )
      assert_equal( c.iv.nil?, true )
    end

    should 'en/decrypt data' do
      c = Nexus::Cipher.new( 'behappy' )
      encrypted = c.encrypt( 'something' )
      cc = Nexus::Cipher.new( 'behappy', c.token )
      cc.iv = c.iv
      plain = cc.decrypt( encrypted )

      assert_equal( plain, 'something' )
    end

    should 'en/decrypt data using the same cipher' do
      c = Nexus::Cipher.new( 'behappy' )
      encrypted = c.encrypt( 'something' )
      plain = c.decrypt( encrypted )

      assert_equal( plain, 'something' )
    end

  end
  
  context 'with token' do

    should 'not create token' do
      c = Nexus::Cipher.new( 'behappy', 
                             "UvChT3jkwD7jXFd8mTWJ087i2Xb3tlGmPWUSYtAiRJM=" )
      assert_equal( c.token, "UvChT3jkwD7jXFd8mTWJ087i2Xb3tlGmPWUSYtAiRJM=" )
      assert_equal( c.iv.nil?, true )
    end

    should 'en/decrypt data' do
      c = Nexus::Cipher.new( 'behappy', 
                             "UvChT3jkwD7jXFd8mTWJ087i2Xb3tlGmPWUSYtAiRJM=" )
      encrypted = c.encrypt( 'something' )
      cc = Nexus::Cipher.new( 'behappy', 
                             "UvChT3jkwD7jXFd8mTWJ087i2Xb3tlGmPWUSYtAiRJM=" )
      cc.iv = c.iv
      plain = cc.decrypt( encrypted )

      assert_equal( plain, 'something' )
    end

    should 'en/decrypt data using the same cipher' do
      c = Nexus::Cipher.new( 'behappy', 
                             "UvChT3jkwD7jXFd8mTWJ087i2Xb3tlGmPWUSYtAiRJM=" )
      encrypted = c.encrypt( 'something' )
      plain = c.decrypt( encrypted )

      assert_equal( plain, 'something' )
    end

  end
end
