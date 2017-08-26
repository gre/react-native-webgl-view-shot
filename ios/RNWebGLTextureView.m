#import <React/RCTConvert.h>
#import "RNWebGLTextureView.h"
#import "GPUImage.h"

@implementation RNWebGLTextureView {
  UIView *view;
  GPUImageFilter *filter;
  GPUImageTextureOutput *output;
  GPUImagePicture *source;
  BOOL continuous;
  BOOL yflip;
  NSTimer *animationTimer;
}

- (instancetype)initWithConfig:(NSDictionary *)config withView:(UIView *)v {
  if ((self = [super initWithConfig:config withWidth:v.bounds.size.width withHeight:v.bounds.size.height])) {
    view = v;
    continuous = [RCTConvert BOOL:[config objectForKey:@"continuous"]];
    yflip = [RCTConvert BOOL:[config objectForKey:@"yflip"]];
    output = [[GPUImageTextureOutput alloc] init];
    output.delegate = self;
    [self snapshot];
    if (continuous) {
      animationTimer =
      [NSTimer scheduledTimerWithTimeInterval:1.0/60.0
                                       target:self
                                     selector:@selector(snapshot)
                                     userInfo:nil
                                      repeats:YES];
    }
  }
  return self;
}


- (void)unload
{
  if (animationTimer) {
    [animationTimer invalidate];
    animationTimer = nil;
  }
}

- (void)snapshot {
  BOOL success;
  CGSize size = view.bounds.size;
  UIGraphicsBeginImageContextWithOptions(size, NO, 0);
  if (yflip) {
    CGAffineTransform flipVertical = CGAffineTransformMake(1, 0, 0, -1, 0, size.height);
    CGContextConcatCTM(UIGraphicsGetCurrentContext(), flipVertical);
  }
  success = [view drawViewHierarchyInRect:(CGRect){CGPointZero, size} afterScreenUpdates:YES];
  UIImage *image = UIGraphicsGetImageFromCurrentImageContext();
  UIGraphicsEndImageContext();
  if (success) {
    if (source) {
      [source removeAllTargets];
    }
    source = [[GPUImagePicture alloc] initWithImage:image];
    [source processImage];
    [source addTarget:output];
  }
}

- (void)newFrameReadyFromTextureOutput:(GPUImageTextureOutput *)callbackTextureOutput
{
  dispatch_async(dispatch_get_main_queue(), ^{
    [self attachTexture:callbackTextureOutput.texture];
    [callbackTextureOutput doneWithTexture];
  });
}


@end
