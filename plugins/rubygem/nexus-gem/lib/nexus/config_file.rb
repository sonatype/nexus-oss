require 'rubygems/local_remote_options'
require 'net/http'
require 'base64'
require 'nexus/cipher'
require 'yaml'

module Nexus
  class ConfigFile

    def initialize( configfile )
      raise 'no file given' unless configfile
      
      @all = {}
      if configfile.is_a?( String )
        @file = configfile
        
        if File.exists?( configfile )
          @all = YAML.load( ::File.read( configfile ) )
        else
          store # make sure we can write it
        end
      elsif configfile
        @file = configfile.file
        @all = configfile.all
      end
    end

    attr_reader :all, :file
    
    def data( repo )
      if repo
        ( @all[ repo ] ||= {} )
      else
        @all
      end
    end
    
    def key?( key, repo = nil )
      data( repo ).key? key
    end
    
    def []( key, repo = nil )
      data( repo )[ key ]
    end
    
    def []=( key, repo, value )
      if value.nil?
        data( repo ).delete( key )
      else
        data( repo )[ key ] = value
      end
    end

    def repos
      all.collect do |k,v|
        k if v.is_a? Hash
      end.select { |s| s }
    end

    def section( key )
      all.dup.select do |k,v|
        if v.is_a? Hash
          v.delete_if { |kk,vv| kk != key }
        else
          k == key
        end
      end
    end

    def delete( *keys )
      delete_map( all, *keys )
      all.each do |k,v|
        delete_map(v, *keys ) if v.is_a? Hash
      end
    end
    
    def delete_map( map, *keys )
      keys.each { |k| map.delete( k ) }
    end
    private :delete_map

    def merge!( other )
      map = other.all
      merge_map( @all, map )
    end
    
    def merge_map( m1, m2 )
      return m2 unless m1
      m2.each do |k,v|
        if v.is_a? Hash
          m1[ k ] ||= {}
          merge_map( m1[ k ], v )
        else
          m1[ k ] = v
        end
      end
    end
    private :merge_map
    
    def store
      dirname = File.dirname( @file )
      Dir.mkdir( dirname ) unless File.exists?( dirname )
      new = !File.exists?( @file )
      
      File.open( @file, 'w') do |f|
        f.write @all.to_yaml
      end
      if new
        File.chmod( 0100600, @file ) rescue nil
      end
    end
  end  
end
