#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html
#
Pod::Spec.new do |s|
  s.name             = 'location_permissions'
  s.version          = '3.0.0+1'
  s.summary          = 'Location permission plugin for Flutter.'
  s.description      = <<-DESC
    This plugin provides a cross-platform (iOS, Android) API to check and request access to the location services on the
    device.
                       DESC
  s.homepage         = 'https://github.com/BaseflowIT/flutter-permission-plugins/tree/develop/packages/location_permissions'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Baseflow' => 'hello@baseflow.com' }
  s.source           = { :path => '.' }
  s.source_files = 'Classes/**/*'
  s.public_header_files = 'Classes/**/*.h'
  s.dependency 'Flutter'

  s.ios.deployment_target = '8.0'
  s.static_framework = true
end

