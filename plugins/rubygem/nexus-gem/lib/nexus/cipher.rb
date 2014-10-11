require 'openssl'
require 'base64'

module Nexus
  class Cipher

    def initialize( pass, token = nil )
      @token = Base64.strict_decode64( token ) if token
      @token ||= OpenSSL::Random.random_bytes( 32 )

      iter = 20000
      key_len = 32
      @key = OpenSSL::PKCS5.pbkdf2_hmac_sha1( pass,
                                              @token,
                                              iter,
                                              key_len )
    end
    
    def cipher
      @c ||= OpenSSL::Cipher::AES.new( 256, :CBC )
    end
    private :cipher

    def token
      Base64.strict_encode64( @token ) 
    end

    def iv
      Base64.strict_encode64( @iv ) if @iv
    end

    def iv=( iv )
      @iv = Base64.strict_decode64( iv )
    end

    def encrypt( data )
      c = cipher
      c.encrypt
      c.key = @key
      @iv = c.random_iv
      Base64.strict_encode64( c.update( data ) + c.final )
    end

    def decrypt( data )
      c = cipher
      c.decrypt
      c.key = @key
      c.iv = @iv
      c.update( Base64.strict_decode64( data ) ) + c.final
    end
  end
end
