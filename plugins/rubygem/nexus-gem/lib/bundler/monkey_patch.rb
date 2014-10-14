require 'bundler'
require 'bundler/fetcher'
require 'bundler/rubygems_integration'
require 'bundler/rubygems_mirror'
module Bundler
  class RubygemsIntegration
    def download_gem(spec, uri, path)
      uri = RubygemsMirror.to_uri(uri)
      Gem::RemoteFetcher.fetcher.download(spec, uri, path)
    end
  end
end
module Bundler
  # Handles all the fetching with the rubygems server
  class Fetcher    
    alias :initialize_old :initialize

    def initialize(remote_uri)
      initialize_old RubygemsMirror.to_uri(remote_uri)
    end
  end
end
