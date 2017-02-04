//
//  UploadFile.m
//  Hello
//
//  Created by mars on 06/12/2016.
//
//

#import "UploadFile.h"
#import "SoundQueueManager.h"

#import "SoundManagerConstant.h"

@interface UploadFile()<SoundQueueManagerDelegate>

@end

@implementation UploadFile

- (void)isRunning:(CDVInvokedUrlCommand*)command
{
    
    [self.commandDelegate runInBackground:^{
        SoundQueueManager *queue = [SoundQueueManager sharedInstance];
        int runing = [queue uploaderRunning];
        
        CDVPluginResult* result = [CDVPluginResult
                                   resultWithStatus:CDVCommandStatus_OK
                                   messageAsInt:runing];
        
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }];
    
}

- (void)dataPath:(CDVInvokedUrlCommand*)command
{
    
    
    [self.commandDelegate runInBackground:^{
        SoundQueueManager *queue = [SoundQueueManager sharedInstance];
        NSString *path = [queue dataPath];
        
        CDVPluginResult* result = [CDVPluginResult
                                   resultWithStatus:CDVCommandStatus_OK
                                   messageAsString:path];
        
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }];
}

- (void)resume:(CDVInvokedUrlCommand*)command
{
    
    
    NSLog(@"%@",DataPath(@""));
    [self.commandDelegate runInBackground:^{
        
        
        NSArray *maArgs = [command arguments];
        NSString* LoginID = [maArgs objectAtIndex:0];
        NSMutableArray *maFiles = [maArgs objectAtIndex:1];
        
        SoundQueueManager *queue = [SoundQueueManager sharedInstance];
        queue.delegate = self;
        [queue startWithUploadFiles:maFiles loginID:LoginID];
        
        int runing = [queue uploaderRunning];
        CDVPluginResult* result = [CDVPluginResult
                                   resultWithStatus:CDVCommandStatus_OK
                                   messageAsInt:runing];
        
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }];
}

#pragma mark - SoundQueueManagerDelegate
- (void)uploadFinishWithCaseID:(NSString *)pCaseID succesful:(BOOL)pSuccesful
{
    
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        
        dispatch_async(dispatch_get_main_queue(), ^{
            NSString *js = [NSString stringWithFormat:@"upload.uploadFinish('%@',%ld);", pCaseID, (NSInteger)pSuccesful];
            
            if ([self.webView isKindOfClass:[UIWebView class]]) {
                [(UIWebView*)self.webView stringByEvaluatingJavaScriptFromString:js];
            }
        });
        
    });
}

- (void)getFilesStatus:(CDVInvokedUrlCommand*)command
{
    
    [self.commandDelegate runInBackground:^{
        
        SoundQueueManager *queue = [SoundQueueManager sharedInstance];
        
        
        CDVPluginResult* result = [CDVPluginResult
                                   resultWithStatus:CDVCommandStatus_OK
                                   messageAsArray:[queue getAllFiles]];
        
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }];
}

- (void)stop:(CDVInvokedUrlCommand*)command
{
    
    [self.commandDelegate runInBackground:^{
        
        SoundQueueManager *queue = [SoundQueueManager sharedInstance];
        
        
        CDVPluginResult* result = [CDVPluginResult
                                   resultWithStatus:CDVCommandStatus_OK
                                   messageAsInt:[queue stop]];
        
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }];
}

@end
