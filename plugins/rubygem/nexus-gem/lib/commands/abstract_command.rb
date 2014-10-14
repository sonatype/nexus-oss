require 'rubygems/local_remote_options'
require 'net/http'
require 'base64'
require 'nexus/config'

class Gem::AbstractCommand < Gem::Command
  include Gem::LocalRemoteOptions

  def initialize( name, summary )
    super
   
    add_option( '-r', '--repo KEY',
                "pick the configuration under that key.\n                                     can be used in conjuction with --clear-repo and the upload itself." ) do |value, options|
      options[ :nexus_repo ] = value
    end

    add_option( '-c', '--clear-repo',
                'Clears the nexus config for the given repo or the default repo' ) do |value, options|
      options[ :nexus_clear ] = value
    end

    # backward compatibility
    add_option( '--nexus-clear', 'DEPRECATED' ) do |value, options|
      options[ :nexus_clear ] = value
    end

    add_option( '--nexus-config FILE',
                "File location of nexus config to use.\n                                     default #{Nexus::Config.default_file}" ) do |value, options|
      options[ :nexus_config ] = File.expand_path( value )
    end
  end

  def url
    url = config.url
    # no leading slash
    url.sub!(/\/$/,'') if url
    url
  end

  def configure_url
    say "Enter the URL of the rubygems repository on a Nexus server"

    url = ask("URL: ")

    if URI.parse( "#{url}" ).host != nil
      config.url = url

      say 'The Nexus URL has been stored in ~/.gem/nexus'
    else
      raise 'no URL given'
    end
  end

  def setup
    prompt_encryption if config.encrypted?
    configure_url if config.url.nil? || options[ :nexus_clear ]
    use_proxy!( url ) if http_proxy( url )
    if( authorization.nil? || 
        config.always_prompt? || 
        options[:nexus_clear] )
      sign_in
    end
  end

  def prompt_encryption
    password = ask_for_password( "Enter your Nexus encryption credentials (no prompt)" )
 
    # recreate config with password
    config.password = password
  end

  def sign_in
    say "Enter your Nexus credentials"
    username = ask("Username: ")
    password = ask_for_password("Password: ")

    # mimic strict_encode64 which is not there on ruby1.8
    token = "#{username}:#{password}"
    auth = "Basic #{Base64.encode64(token).gsub(/\s+/, '')}"
    @authorization = token == ':' ? nil : auth
     
    unless config.always_prompt?
      config.authorization = @authorization
      if @authorization
        say "Your Nexus credentials has been stored in #{config}"
      else
        say "Your Nexus credentials has been deleted from #{config}"
      end
    end
  end

  def this_config( pass = nil )
    Nexus::Config.new( options[ :nexus_config ],
                       options[ :nexus_repo ] )
  end
  private :this_config
  
  def config( pass = nil )
    @config = this_config( pass ) if pass
    @config ||= this_config
  end

  def authorization
    @authorization || config.authorization
  end

  def make_request(method, path)
    require 'net/http'
    require 'net/https'

    url = URI.parse( "#{self.url}/#{path}" )

    http = proxy_class.new( url.host, url.port )

    if url.scheme == 'https'
      http.use_ssl = true
    end
    
    #Because sometimes our gems are huge and our people are on vpns
    http.read_timeout = 300

    request_method =
      case method
      when :get
        proxy_class::Get
      when :post
        proxy_class::Post
      when :put
        proxy_class::Put
      when :delete
        proxy_class::Delete
      else
        raise ArgumentError
      end

    request = request_method.new( url.path )
    request.add_field "User-Agent", "Ruby" unless RUBY_VERSION =~ /^1.9/

    yield request if block_given?
    
    if Gem.configuration.verbose.to_s.to_i > 0
      warn "#{request.method} #{url.to_s}"
      if config.authorization
        warn 'use authorization' 
      else
        warn 'no authorization'
      end

      if http.proxy_address
        warn "use proxy at #{http.proxy_address}:#{http.proxy_port}"
      end
    end

    http.request(request)
  end

  def use_proxy!( url )
    proxy_uri = http_proxy( url )
    @proxy_class = Net::HTTP::Proxy( proxy_uri.host,
                                     proxy_uri.port,
                                     proxy_uri.user,
                                     proxy_uri.password )
  end

  def proxy_class
    @proxy_class || Net::HTTP
  end

  # @return [URI, nil] the HTTP-proxy as a URI if set; +nil+ otherwise
  def http_proxy( url )
    uri = URI.parse( url ) rescue nil
    return nil if uri.nil?
    if no_proxy = ENV[ 'no_proxy' ] || ENV[ 'NO_PROXY' ]
      # does not look on ip-adress ranges
      return nil if no_proxy.split( /, */ ).member?( uri.host )
    end
    key = uri.scheme == 'http' ? 'http_proxy' : 'https_proxy'
    proxy = Gem.configuration[ :http_proxy ] || ENV[ key ] || ENV[ key.upcase ]
    return nil if proxy.nil? || proxy == :no_proxy

    URI.parse( proxy )
  end

  def ask_for_password(message)
    system "stty -echo"
    password = ask(message)
    system "stty echo"
    ui.say("\n")
    password
  end
end
