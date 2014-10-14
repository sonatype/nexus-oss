require 'nexus/cipher'
require 'nexus/config_file'

module Nexus
  class Config

    def self.default_file
      File.join( Gem.user_home, '.gem', 'nexus' )
    end

    def initialize( file = nil, repo = nil )
      @repo = repo
      @conf = ConfigFile.new( file || self.class.default_file )
      @secr = ConfigFile.new( @conf[ :secrets ] ) if @conf.key? :secrets
    end

    private

    def encrypt_or_decrypt_credentials
      map = config.all
      yield map if map[ :authorization ]
      map.each do |k, v|
        yield v if v.is_a?( Hash ) && v[ :authorization ]
      end
    end

    def move_credentials( from, to )
      keys = [ :secrets, :token, :iv, :authorization ]
      ([ nil ] + from.repos ).each do |repo|
        keys.each do |k|
          to[ k, repo ] = from[ k, repo ]
          from[ k, repo ] = nil
        end
      end
    end

    def config
      @map ||= @secr ? @secr : @conf
    end

    def key?( key )
      config.key?( key, @repo )
    end

    def []( key )
      config[ key, @repo ]
    end

    def []=( key, value )
      config[ key, @repo ] = value
    end

    public

    def encrypted?
      config.key?( :token )
    end

    def password=( pass )
      @cipher = Cipher.new( pass, config[ :token ] )
    end

    def always_prompt?
      @conf[ :always_prompt ]
    end

    def clear_always_prompt
      @conf.delete( :always_prompt )
      @conf.store
    end

    def clear_credentials( store = true )
      secrets = @conf[ :secrets ]
      if secrets && !config.key?( :token )
        FileUtils.rm_f( secrets )
        @map = @conf
      else
        config.delete :iv, :authorization
      end
      @conf.delete( :secrets )
      @conf.store if store
    end

    def always_prompt
      @conf[ :always_prompt, nil ] = true
      
      config.delete( :token )

      clear_credentials( false )

      @conf.store
    end

    def decrypt_credentials
      unless encrypted?
        warn 'not encrypted - nothing to do'
        return
      end
      encrypt_or_decrypt_credentials do |c|
        @cipher.iv = c[ :iv ]
        c[ :authorization ] = @cipher.decrypt( c[ :authorization ] )
        c.delete( :iv )
      end
      config.all.delete( :token )
      config.store
      @cipher = nil
    end
    
    def encrypt_credentials
       if encrypted?
         warn 'already encrypted - nothing to do'
         return
       end
      encrypt_or_decrypt_credentials do |c|
        c[ :authorization ] = @cipher.encrypt( c[ :authorization ] )
        c[ :iv ] = @cipher.iv
      end
      config.all[ :token ] = @cipher.token
      config.store
    end
        
    def new_secrets( new )
      old = @conf.all[ :secrets ]
      if old and new
        FileUtils.mv( old, new )
      end
      if new
        @secr = ConfigFile.new( new )
      end
        
      if new.nil? && old
        @conf.merge!( @secr )
        FileUtils.rm_f( old )
        @secr = nil
      end

      if old.nil? and new
        move_credentials( @conf, @secr )

        @secr.store
      end

      # store the new location
      @conf[ :secrets, nil ] = new
      @conf.store
    end

    def repos
      result = @conf.section( :url )
      if url = result.delete( :url )
        result[ 'DEFAULT' ] = url
      end
      result.keys.each do |key|
        if key != 'DEFAULT'
          result[ key ] = result[ key ][ :url ]
        end
      end
      result
    end

    def authorization
      auth = self[ :authorization ]
      if @cipher && auth && self[ :iv ]
        @cipher.iv = self[ :iv ]
        @cipher.decrypt( auth )
      elsif @cipher && auth
        authorization = auth
      else
        auth
      end
    end

    def authorization=( auth )
      if @cipher && auth
        self[ :authorization ] = @cipher.encrypt( auth )
        self[ :iv ] = @cipher.iv
      else
        self[ :authorization ] = auth
      end
      config.store
      auth
    end

    def url
      @conf[ :url, @repo ]
    end

    def url=( u )
      @conf[ :url, @repo ] = u
      @conf.store
    end

    def to_s
      config.file
    end
  end
end
