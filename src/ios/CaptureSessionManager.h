
//https://github.com/jj0b/AROverlayImageCapture/
#import <AVFoundation/AVFoundation.h>
#import <UIKit/UIKit.h>
#import <AssetsLibrary/AssetsLibrary.h>

#define kImageCapturedSuccessfully @"imageCapturedSuccessfully"
#define kVideoCapturedSuccessfully @"videoCapturedSuccessfully"

@interface CaptureSessionManager : NSObject <AVCaptureFileOutputRecordingDelegate> {
    
}

@property (retain) AVCaptureVideoPreviewLayer   *previewLayer;
@property (retain) AVCaptureSession             *captureSession;
@property (retain) AVCaptureStillImageOutput    *stillImageOutput;
@property (nonatomic, retain) UIImage           *stillImage;
@property (retain) AVCaptureMovieFileOutput     *movieFileOutput;
@property (nonatomic, retain) NSURL             *movieFile;

- (void)addVideoPreviewLayer;
- (void)addStillImageOutput;
- (void)captureStillImage;
- (void)addVideoInputFrontCamera:(BOOL)front;
- (void)stopRunning;

- (void)startRecording;
- (void)stopRecording;
- (void)addMovieFileOutputWith:(Float64)fps MaxDuration:(int32_t)duration;

@end