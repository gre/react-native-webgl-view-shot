#import <UIKit/UIKit.h>
#import "RNWebGLTexture.h"
#import "GPUImage.h"

@interface RNWebGLTextureView: RNWebGLTexture <GPUImageTextureOutputDelegate>
- (instancetype)initWithConfig:(NSDictionary *)config withView:(UIView *)view;
@end
