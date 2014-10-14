module Nexus
  class Check

    def check_gemspec_rz(gemfile, gemspec)
      spec_from_gem = Gem::Package.new( gemfile ).spec
      spec_from_gemspec = Marshal.load( Gem.inflate( Gem.read_binary( gemspec) ) )
      spec_from_gem == spec_from_gemspec
    end

    def check_spec_name( gemfile )
      Gem::Format.from_file_by_path( gemfile ).spec.name
    end

    def specs_size( specsfile )
      specs = Marshal.load( Gem.read_binary( specsfile ) )
      specs.size
    end

  end
end
Nexus::Check.new
