require 'maven/tools/pom'
require 'json'
require 'nexus/indexer'
require 'nexus/dependencies'
require 'nexus/dependency_helper_impl'
require 'nexus/gemspec_helper_impl'

module Nexus
  class Rubygems

    def new_dependency_helper
      Nexus::DependencyHelperImpl.new
    end

    def new_gemspec_helper( file )
      Nexus::GemspecHelperImpl.from_gemspec_rz( file )
    end

    def new_gemspec_helper_from_gem( file )
      Nexus::GemspecHelperImpl.from_gem( file )
    end

    def recreate_rubygems_index( directory )
      indexer = Nexus::Indexer.new( directory )
      indexer.generate_index
      indexer.remove_tmp_dir

      # delete obsolete files
      Dir[ File.join( directory, '*' ) ].each do |f|
        if !f.match( /.*specs.#{Gem.marshal_version}.gz/ ) && !File.directory?( f )
          FileUtils.rm_f( f )
        end
      end

      # NOTE that code gave all kinds of result but the expected
      #      could be jruby related or not.
      #      just leave the permissions as they are
      #
      # fix permissions 
      # mode = 16877 # File.new( directory ).stat.mode # does not work with jruby
      # ( [ directory ] + Dir[ File.join( directory, '**', '*') ] ).each do |f|
      #   begin
      #     if File.directory? f
      #       FileUtils.chmod( mode, f )
      #     end
      #   rescue
      #     # well - let it as it is
      #   end
      # end
      nil
    end

    def purge_broken_depencency_files( directory )
      Dir[ File.join( directory, 
                      'api', 'v1', 'dependencies', 
                      '*' ) ].each do |file|
        begin
          if File.file?( file ) and file =~ /.json.rz$/
            Marshal.load( File.read( file ) )
          else
            FileUtils.rm_rf( file )
          end
        rescue
          # just in case the file is directory delete it as well
          FileUtils.rm_rf( file )
        end
      end
      nil
    end

    def purge_broken_gemspec_files( directory )
      Dir[ File.join( directory, 
                      'quick', "Marshal.#{Gem.marshal_version}",
                      '*', '*' ) ].each do |file|
        begin
          Marshal.load( Gem.inflate( Gem.read_binary( file ) ) )
        rescue
          # just in case the file is directory delete it as well
          FileUtils.rm_rf( file )
        end
      end
      nil
    end

    def create_quick( spec )
      Gem.deflate( Marshal.dump( spec ) ).bytes.to_a
    end

    def spec_get( gemfile )
      load_spec( gemfile )
    end

    def load_spec( gemfile )
      case gemfile
      when String
        Gem::Package.new( gemfile ).spec
      else
        io = StringIO.new( read_binary( gemfile ) )
        # this part if basically copied from rubygems/package.rb
        Gem::Package::TarReader.new( io ) do |reader|
          reader.each do |entry|
            case entry.full_name 
            when 'metadata' then
              return Gem::Specification.from_yaml entry.read
            when 'metadata.gz' then
              args = [entry]
              args << { :external_encoding => Encoding::UTF_8 } if
                Object.const_defined?(:Encoding) &&
                Zlib::GzipReader.method(:wrap).arity != 1
              
              Zlib::GzipReader.wrap(*args) do |gzio|
                return Gem::Specification.from_yaml gzio.read
              end              
            end
          end
        end
        raise "failed to load spec from #{gemfile}"
      end
    end

    def to_pom( spec_source, snapshot = false )
      spec = Marshal.load( Gem.inflate( read_binary( spec_source ) ) )
      proj = Maven::Tools::POM.new( spec, snapshot )
      proj.to_s
    end

    %W(name_preversions_map name_versions_map).each do |method|

      self.class_eval <<-EVAL
        def #{method}( source, modified )
          if @#{method}.nil? || @#{method}_modified != modified
            specs = load_specs( source )
            @#{method} = {}
            specs.select do |s|
              v = @#{method}[ s[0].to_s ] ||= []
              v << "\#{s[1]}-\#{s[2]}"
            end
            @#{method}_modified = modified
          end
          @#{method}
        end
      EVAL

    end

    def dependencies( name, file )
      data = Marshal.load( read_binary( file ) )
      Dependencies.new( name, data )
    end

    # TODO still needed ?
    def list_all_versions( name, source, modified, prerelease = false )
      map_method = prerelease ? :name_preversions_map : :name_versions_map
      send( map_method, source, modified )[ name.to_s ] || []
    end

    def empty_specs
      dump_specs( [] )
    end

    def merge_specs( sources, lastest = false )
      result = []
      sources.each do |s|
        result += load_specs( s )
      end
      result = regenerate_latest( result ) if lastest
      dump_specs( result )
    end

    def regenerate_latest( specs )
      specs.sort!
      specs.uniq!
      map = {}
      specs.each do |s|
        list = map[ s[ 0 ] ] ||= []
        list << s
      end
      result = []
      map.each do |name, list|
        list.sort!
        list.uniq!
        lastest_versions = {}
        list.each do |i|
          version = i[1]
          platform = i[2]
          lastest_versions[ platform ] = i
        end
        result += lastest_versions.collect { |k, v| v }
      end
      result
    end
    private :regenerate_latest

    def add_spec( spec, source, type )
      case type.downcase.to_sym
      when :latest
        do_add_spec( spec, source, true )
      when :release
        # refill the map
        @name_versions_map = nil
        do_add_spec( spec, source ) unless spec.version.prerelease?
      when :prerelease
        # refill the map
        @name_preversions_map = nil
        do_add_spec( spec, source ) if spec.version.prerelease?
      end
    end

    def delete_spec( spec, source, releases = nil )
      # refill the map
      @name_versions_map = nil
      @name_preversions_map = nil
      specs = load_specs( source )
      old_entry = [ spec.name, spec.version, spec.platform.to_s ]
      if specs.member? old_entry
        specs.delete old_entry
        if releases
          releases_specs = load_specs( releases )
          releases_specs.delete old_entry
          specs = regenerate_latest( releases_specs )
        end
        dump_specs( specs )
      end
    end

    private

    def ensure_latest( spec, ref_source )
      ref = load_specs( ref_source )
      map = {}
      ref.each do |s|
        if s[ 0 ] == spec.name  and ( s[1] != spec.version or s[2].to_s != spec.platform.to_s )
          a = map[ s[1] ] ||= []
          a << s
        end
      end
      k = map.keys.sort.last
      map[ k ] || []
    end

    def do_add_spec( spec, source, latest = false )
      specs = load_specs( source )
      new_entry = [ spec.name, spec.version, spec.platform.to_s ]
      unless specs.member?( new_entry )
        if latest
          new_specs = regenerate_latest( specs + [ new_entry ] )
          dump_specs( new_specs ) if new_specs != specs
        else
          specs << new_entry
          dump_specs( specs )
        end
      end
    end

    def read_binary( io )
      case io
      when String
        Gem.read_binary( io )
      else
        result = []
        while ( ( b = io.read ) != -1 ) do
          result << b
        end
        result.pack 'C*'
      end
    ensure
      io.close if io.respond_to? :close
    end

    def load_specs( source )
      Marshal.load read_binary( source )
    end

    def dump_specs( specs )
      specs.uniq!
      specs.sort!
      Marshal.dump( compact_specs( specs ) ).bytes.to_a
    end

    def compact_specs( specs )
      names = {}
      versions = {}
      platforms = {}

      specs.map do |( name, version, platform )|
        names[ name ] = name unless names.include? name
        versions[ version ] = version unless versions.include? version
        platforms[ platform ] = platform unless platforms.include? platform

        [ names[ name ], versions[ version ], platforms[ platform ] ]
      end
    end
  end
end
Nexus::Rubygems
