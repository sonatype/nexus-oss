require 'minitest/autorun'
require 'shoulda'
require 'fileutils'
require 'nexus/config_file'

class ConfigTest < ::MiniTest::Unit::TestCase
  include ShouldaContextLoadable 
  
  context 'file' do

    should 'store key/values' do
      file = File.join( 'pkg', 'cfg' )
      FileUtils.rm_f file
      f = Nexus::ConfigFile.new( file )
      f[ 'asd_key', nil ] = 'dsa_value'
      f[ 'asdasd_key', nil ] = 'dsa_value_dsa'
      f[ 'asd_key', 'first' ] = 'dsa_value_dsa'
      f[ 'asdasd_key', 'first' ] = 'dsadsa_value'

      assert_equal( f[ 'asd_key', nil ], 'dsa_value' )
      assert_equal( f[ 'asdasd_key', 'first' ], 'dsadsa_value' )

      f.store

      assert_equal( f.all, Nexus::ConfigFile.new( file ).all )
      assert_equal( f.repos, [ 'first' ] )

      assert_equal( f.section( 'asd_key' ),
                    { "asd_key"=>"dsa_value", 
                      "first"=>{"asd_key"=>"dsa_value_dsa"} } )
    end

    should 'delete key/values' do
      file = File.join( 'pkg', 'cfg' )
      FileUtils.rm_f file
      f = Nexus::ConfigFile.new( file )
      f[ 'asd_key', nil ] = 'dsa_value'
      f[ 'asdasd_key', nil ] = 'dsa_value_dsa'
      f[ 'asd_key', 'first' ] = 'dsa_value_dsa'
      f[ 'asdasd_key', 'first' ] = 'dsadsa_value'

      assert_equal( f[ 'asd_key', nil ], 'dsa_value' )
      assert_equal( f[ 'asdasd_key', 'first' ], 'dsadsa_value' )

      f.delete( 'asd_key' )

      assert_equal( f[ 'asd_key', nil ], nil )
      assert_equal( f[ 'asd_key', 'first' ], nil )
      assert_equal( f[ 'asdasd_key', nil ], 'dsa_value_dsa' )
      assert_equal( f[ 'asdasd_key', 'first' ], 'dsadsa_value' )
    end

    should 'merge other file' do
      file1 = File.join( 'pkg', 'cfg1' )
      file2 = File.join( 'pkg', 'cfg2' )
      FileUtils.rm_f file1
      FileUtils.rm_f file2
      f1 = Nexus::ConfigFile.new( file1 )
      f2 = Nexus::ConfigFile.new( file2 )

      assert_equal( f1.all, {} )

      f2[ 'asd_key', nil ] = 'dsa_value'
      f2[ 'asdasd_key', 'first' ] = 'dsadsa_value'

      f1.merge!( f2 )

      assert_equal( f1[ 'asd_key', nil ], 'dsa_value' )
      assert_equal( f1[ 'asdasd_key', 'first' ], 'dsadsa_value' )

      f1.store
      f2.store

      assert_equal( Nexus::ConfigFile.new( file1 ).all, 
                    Nexus::ConfigFile.new( file2 ).all )
    end
  end
end
