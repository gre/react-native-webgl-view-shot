#import <React/RCTConvert.h>
#import <React/RCTImageSource.h>
#import <React/RCTBridge.h>
#import <React/RCTUIManager.h>
#if __has_include(<React/RCTUIManagerUtils.h>)
#import <React/RCTUIManagerUtils.h>
#endif
#import "RNWebGLTextureViewLoader.h"
#import "RNWebGLTextureView.h"

@implementation RNWebGLTextureViewLoader

RCT_EXPORT_MODULE()

@synthesize bridge = _bridge;

- (BOOL)canLoadConfig:(NSDictionary *)config {
  return [config objectForKey:@"view"] != nil;
}

- (void)loadWithConfig:(NSDictionary *)config
   withCompletionBlock:(RNWebGLTextureCompletionBlock)callback {
  dispatch_async(RCTGetUIManagerQueue(), ^{
    NSNumber *viewTag = [RCTConvert NSNumber:[config objectForKey:@"view"]];
    [self.bridge.uiManager addUIBlock:^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, UIView *> *viewRegistry) {
      UIView *view = viewRegistry[viewTag];
      if (!view) {
        callback([NSError errorWithDomain:@"RNWebGLViewShot" code:1 userInfo:@{ NSLocalizedDescriptionKey: [NSString stringWithFormat:@"No view found with reactTag: %@", viewTag] }], nil);
      }
      else {
        RNWebGLTextureView *obj = [[RNWebGLTextureView alloc] initWithConfig:config withView:view];
        callback(nil, obj);
      }
    }];
  });
}

@end
