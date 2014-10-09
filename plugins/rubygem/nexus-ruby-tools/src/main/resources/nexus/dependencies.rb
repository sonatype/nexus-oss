
module Nexus
  class Dependencies
    
    attr_reader :name

    def initialize( name, data )
      @name = name
      @versions = {}
      data.sort do |m,n|
        Gem::Version.new(m[:number]) <=> Gem::Version.new(n[:number])
      end.select do |d|
        is_ruby = d[:platform].downcase =~ /ruby/ 
        is_java = d[:platform].downcase =~ /(java|jruby)/ 
        if is_ruby
          @versions[ d[:number] ] ||= d[:platform]
        end
        if is_java
          # java overwrites since it has higher prio
          @versions[ d[:number] ] = d[:platform]
        end
      end
    end
    
    def versions( prereleases )
      if prereleases
        @versions.keys.select { |v| v =~ /[a-zA-Z]/ }
      else
        @versions.keys.select { |v| ! ( v =~ /[a-zA-Z]/ ) }
      end
    end

    def platform( version )
      @versions[ version ]
    end
  end
end
