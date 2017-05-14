Pod::Spec.new do |s|
  s.name             = 'FBNotifications'
  s.version          = '1.0.2'
  s.license          =  { :type => 'Facebook Platform License', :file => 'LICENSE' }
  s.summary          = 'Facebook In-App Notifications Framework'
  s.homepage         = 'https://developers.facebook.com/products/analytics'
  s.authors          = { 'Nikita Lutsenko' => 'nlutsenko@me.com' }
  
  s.source       = { :git => 'https://github.com/MobileConcepts/FBNotifications.git', :tag => s.version.to_s }

  s.requires_arc = true

  s.dependency 'SDWebImage'
  
  s.ios.deployment_target = '8.0'
  
  s.source_files = 'iOS/FBNotifications/FBNotifications/**/*.{h,m}'
  s.public_header_files = 'iOS/FBNotifications/FBNotifications/*.h'

  s.frameworks = 'ImageIO', 'MobileCoreServices','SDWebImage'
end
