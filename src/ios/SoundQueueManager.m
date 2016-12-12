//
//  SoundQueueManager.m
//  AFSoundManager-Demo
//
//  Created by mars on 16/7/7.
//  Copyright © 2016年 AlvaroFranco. All rights reserved.
//
//Weak
#define MLWeakObject(obj) __typeof__(obj) __weak
#define MLWeakSelf MLWeakObject(self)

#define kCaseIDKey @"caseID"
#define kIndexKey @"Index"

#define kUpload @"upload"
#define kRetrieve @"retrieve"

#import "SoundManagerConstant.h"

#import "SoundQueueManager.h"

@interface SoundQueueManager()
{
    NSMutableArray *_maSound;
    NSTimer *_timer;
    NSString *_loginID;
}
@end

@implementation SoundQueueManager
#pragma mark - singleton

+ (SoundQueueManager *)sharedInstance
{
    static SoundQueueManager *sharedInstance = nil;
    static dispatch_once_t onceToken;
    
    dispatch_once(&onceToken, ^{
        sharedInstance = [[SoundQueueManager alloc] init];
    });
    
    return sharedInstance;
}

- (instancetype)init
{
    if ([super init]) {
        
    }
    return self;
}

#pragma mark - local data
- (NSString *)dataPath
{
    return DataPath(@"");
}

- (NSMutableArray *)scanPath:(NSString *)sPath {
    
    NSMutableArray *result = [NSMutableArray array];
    BOOL isDir;
    
    [[NSFileManager defaultManager] fileExistsAtPath:sPath isDirectory:&isDir];
    
    if(isDir)
    {
        NSArray *contentOfDirectory=[[NSFileManager defaultManager] contentsOfDirectoryAtPath:sPath error:NULL];
        int i;
        for(i=0;i<contentOfDirectory.count;i++)
        {
            NSString *fileName = [contentOfDirectory objectAtIndex:i];
            NSDictionary *dicFileInfo = [self validateWithFileName:fileName];
            if (dicFileInfo) {
                [result addObject:dicFileInfo];
            }
        }
        
    }
    return result;
}

- (NSDictionary *)validateWithFileName:(NSString *)pFileName
{
    NSDictionary *dic = nil;
    NSString *splitMark = @"_";
    if ([pFileName rangeOfString:splitMark].location != NSNotFound) {
        NSArray *arr = [pFileName componentsSeparatedByString:splitMark];
        if (arr.count == 2) {
            
            NSCharacterSet* notDigits = [[NSCharacterSet decimalDigitCharacterSet] invertedSet];
            if ([arr[0] rangeOfCharacterFromSet:notDigits].location == NSNotFound && [arr[1] rangeOfCharacterFromSet:notDigits].location == NSNotFound)
            {
                dic = @{kCaseIDKey:arr[0],
                        kIndexKey:arr[1]};
            }
            
        }
    }
    return dic;
}

- (void)completeTaskWithCaseID:(NSString *)pCaseID index:(NSInteger)pIndex
{
    NSString *fileName = [NSString stringWithFormat:@"%@_%ld", pCaseID, pIndex];
    if ([[NSFileManager defaultManager] fileExistsAtPath:DataPath(fileName)]){
        
        if (![[NSFileManager defaultManager] fileExistsAtPath:FinishDataPath(@"")]) {
            [[NSFileManager defaultManager] createDirectoryAtPath:FinishDataPath(@"")
                                      withIntermediateDirectories:NO
                                                       attributes:nil
                                                            error:nil];
        }
        
        if ([[NSFileManager defaultManager] fileExistsAtPath:FinishDataPath(fileName)]){
            [[NSFileManager defaultManager] removeItemAtPath:DataPath(fileName) error:nil];
        }
        else if ([[NSFileManager defaultManager] copyItemAtPath:DataPath(fileName)
                                                    toPath:FinishDataPath(fileName)
                                                    error:nil]) {
            [[NSFileManager defaultManager] removeItemAtPath:DataPath(fileName) error:nil];
        }
        
        
    }
    
}

- (void)setSessionStatusWithStatus:(SessionStatus)pStatus
{
    Uploader *uploader = [Uploader sharedInstance];
    [uploader setSessionStatusWithStatus:pStatus];
}

- (BOOL)uploaderRunning
{
    return [_timer isValid];
}

- (void)resumeWithLoginID:(NSString *)pLogninID
{
    if (pLogninID.length == 0) {
        NSLog(@"invalid loginID! launch loading failed!");
        return;
    }
    _loginID = pLogninID;
    
    if (_timer) {
        [_timer invalidate];
        _timer = nil;
    }
    
    if (![[NSFileManager defaultManager] fileExistsAtPath:DataPath(@"")]) {
        [[NSFileManager defaultManager] createDirectoryAtPath:DataPath(@"")
                                  withIntermediateDirectories:NO
                                                   attributes:nil
                                                        error:nil];
    }
    
    _timer = [NSTimer scheduledTimerWithTimeInterval:5
                                                     target:self
                                                   selector:@selector(upload)
                                                   userInfo:nil
                                                    repeats:YES];

}

- (void)upload
{
    MLWeakSelf weakSelf = self;
    
    Uploader *uploader = [Uploader sharedInstance];
    
    if (_loginID.length == 0) {
        NSLog(@"invalid loginID! launch loading failed!");
        return;
    }
    
    [uploader setLoginID:_loginID];
    
    if ([uploader sessionStatus] == SessionStatusAvailable) {
        _maSound = [self scanPath:DataPath(@"")];
    }
    
    if ([uploader sessionStatus] == SessionStatusAvailable && _maSound.count != 0) {
        [uploader setSessionStatusWithStatus:SessionStatusBusy];
        NSDictionary *uploadTask = [_maSound objectAtIndex:0];
        
        [uploader addTaskToQueueWithUploadTask:uploadTask[kCaseIDKey]
                                         index:[uploadTask[kIndexKey] integerValue]
                               completionBlock:^(TaskStatus taskStatus) {
                                   
                                   if (taskStatus == TaskStatusUploadFinished) {
                                       [weakSelf completeTaskWithCaseID:uploadTask[kCaseIDKey]
                                                                  index:[uploadTask[kIndexKey] integerValue]];
                                       
                                   }
                                   
                                   [weakSelf setSessionStatusWithStatus:SessionStatusAvailable];
                               }];
    }
    else{
//        NSLog(@"[uploader sessionStatus]:%ld, no available", [uploader sessionStatus]);
    }
}
@end

typedef  void(^completionURLSessionBlock)(NSString *message, NSInteger total, NSInteger current, NSError *error);
typedef void(^completionUPloadBlock)(BOOL result);
@interface Uploader()<NSURLSessionDelegate>
{
    NSURLSession *_session;
    SessionStatus _sessionStatus;
    completionURLSessionBlock _completionBlock;
    completionUPloadBlock _completionUploadBlock;
    NSMutableArray *_maUploadingQueueForTask;
    NSString *_loginID;
}

@property (nonatomic, strong) NSMutableDictionary *responsesData;

@end
@implementation Uploader

static NSString * const kBackgroundRefreshIdentifier = @"com.delawareconsulting.backgroundupload";

+ (Uploader *)sharedInstance
{
    static Uploader *sharedInstance = nil;
    static dispatch_once_t onceToken;
    
    dispatch_once(&onceToken, ^{
        sharedInstance = [[Uploader alloc] init];
    });
    
    return sharedInstance;
}

- (instancetype)init
{
    if ([super init]) {
        
    }
    return self;
}

- (void)setLoginID:(NSString *)pLoginID
{
    _loginID = pLoginID;
}

- (SessionStatus)sessionStatus
{
    return _sessionStatus;
}

- (void)setSessionStatusWithStatus:(SessionStatus)pSessionStatus
{
    _sessionStatus = pSessionStatus;
}

- (void)addTaskToQueueWithUploadTask:(NSString *)pUploadTask
                               index:(NSInteger)pIndex
                     completionBlock:(void(^)(TaskStatus taskStatus))pCompletionBlock
{
    
    [self retrieveUploadedSizeWithIdentifier:pUploadTask
                                       index:pIndex
                             completionBlock:^(NSInteger uploadedSize, NSError *error) {
                                 
                                 if (error) {
                                     pCompletionBlock(TaskStatusUploadFailed);
                                 }
                                 else{
                                     
                                     [self prepareUploadTaskWithTaskIdentifier:pUploadTask
                                                                 startPosition:uploadedSize
                                                                         index:pIndex
                                                               completionBlock:^(BOOL result) {
                                                                   if (result) {
                                                                       pCompletionBlock(TaskStatusUploadFinished);
                                                                   }
                                                                   else{
                                                                       pCompletionBlock(TaskStatusUploadFailed);
                                                                   }
                                                               }];
                                 }
                             }];

}

- (void)prepareUploadTaskWithTaskIdentifier:(NSString *)pTaskIdentifier
                              startPosition:(NSInteger)pStartPosition
                                      index:(NSInteger)pIndex
                            completionBlock:(void(^)(BOOL result))pCompletionBlock
{
    
    NSString *fileName = [NSString stringWithFormat:@"%@_%ld", pTaskIdentifier, pIndex];
    if (![[NSFileManager defaultManager] fileExistsAtPath:DataPath(fileName)] || ![[NSFileManager defaultManager] isReadableFileAtPath:DataPath(fileName)]){
        pCompletionBlock(NO);
        return;
    }
    
    NSData *fullData = [NSData dataWithContentsOfFile:DataPath(fileName)];
    
    NSLog(@"%@", DataPath(pTaskIdentifier));
    NSData *data = [fullData componentsSeparatedFromByte:pStartPosition];
    _maUploadingQueueForTask = [data componentsSeparatedByChunkSize:(kBlockSize * 1024)];
    
    if (!data) {
        pCompletionBlock(YES);
        return;
    }
    
    __block __weak void (^weak_next)(NSInteger);
    void (^next)(NSInteger);
    weak_next = next = ^(NSInteger index) {
        if (index == _maUploadingQueueForTask.count - 1) {
            NSLog(@"end");
            pCompletionBlock(YES);
            return;
            
        }
        __block NSInteger i = index;
        NSLog(@"%ld", i);
        
        void(^inner_block)(NSInteger) = weak_next;
        
        [self uploadOnePartWithTaskIdentifier:pTaskIdentifier
                                        index:pIndex
                                   base64Data:[_maUploadingQueueForTask[i] base64EncodedString]
                                contentLength:fullData.length
                              completionBlock:^(BOOL result) {
                                  if (result) {
                                      inner_block(++i);
                                  }
                                  else{
                                      
                                      pCompletionBlock(result);
                                  }
                           }];
        
        
    };
    next(0);
    
    
}

#pragma mark - data access
- (void)retrieveUploadedSizeWithIdentifier:(NSString *)pIdetifier
                                     index:(NSInteger)pIndex
                           completionBlock:(void(^)(NSInteger uploadedSize, NSError *error))pCompletionBlock
{
    NSDictionary *dicParam = nil;
    NSDictionary *dicDetail = @{@"CaseID":pIdetifier,
                                @"Index":@(pIndex),};
    dicParam = @{@"Requests":dicDetail};
    
    [self urlSessionManager:@""
                     params:dicParam
                   isUpload:NO
              contentLength:0
            completionBlock:^(NSString *message, NSInteger current, NSInteger total, NSError *error) {
                if (!error) {
                    pCompletionBlock(current, error);
                }
                else{
                    pCompletionBlock(0, error);
                }
            }];
    
}

- (void)uploadOnePartWithTaskIdentifier:(NSString *)pTaskIdentifier
                                  index:(NSInteger)pIndex
                             base64Data:(NSString *)pBase64Data
                          contentLength:(NSInteger)pContentLength
                        completionBlock:(void(^)(BOOL result))pCompletionBlock
{
    
    
    NSDictionary *dicParam = nil;
    NSDictionary *dicDetail = @{@"CaseID":pTaskIdentifier,
        @"LoginID":_loginID,
        @"Index":@(pIndex),
        @"CustomerSatisfactionVoiceFile":pBase64Data};
    dicParam = @{@"Requests":dicDetail};
    
    [self urlSessionManager:pTaskIdentifier
                     params:dicParam
                   isUpload:YES
              contentLength:pContentLength
            completionBlock:^(NSString *message, NSInteger total, NSInteger current, NSError *error) {

                if ([message isEqualToString:@"OK"]) {
                    pCompletionBlock(YES);
                }
                else{
                    pCompletionBlock(NO);
                }
                
            }];
}

- (void)urlSessionManager:(NSString *)pPath
                   params:(NSDictionary *)pParams
                 isUpload:(BOOL)pIsUpload
            contentLength:(NSInteger)pContentLength
          completionBlock:(void(^)(NSString *message, NSInteger total, NSInteger current, NSError *error))pCompletionBlock
{
    
    
    NSError *error;
    
    _session = [NSURLSession sessionWithConfiguration:[NSURLSessionConfiguration defaultSessionConfiguration] delegate:self delegateQueue:nil];
    
    NSURL *url = nil;
    if (pIsUpload) {
        url = [NSURL URLWithString:[NSString stringWithFormat:@"%@%@",kBaseURL,kUploadVoiceFile]];
        
    }
    else{
        url = [NSURL URLWithString:[NSString stringWithFormat:@"%@%@",kBaseURL,kGetUploadVoiceFileProgress]];
    }

    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:url
                                                           cachePolicy:NSURLRequestUseProtocolCachePolicy
                                                       timeoutInterval:20.0];
    
    [request addValue:@"application/json" forHTTPHeaderField:@"Content-Type"];
    [request addValue:kApiKey forHTTPHeaderField:@"API-Key"];
    if (pIsUpload) {
        [request addValue:[NSString stringWithFormat:@"%ld", pContentLength] forHTTPHeaderField:@"Total-Length"];
        
    }
    
    [request setHTTPMethod:@"POST"];
    
    
    NSData *postData = [NSJSONSerialization dataWithJSONObject:pParams options:0 error:&error];
    [request setHTTPBody:postData];
    
    _completionBlock = pCompletionBlock;
    
    NSURLSessionTask *postDataTask = [_session dataTaskWithRequest:request];
    if (pIsUpload) {
        [postDataTask setTaskDescription:kUpload];
    }
    else{
        [postDataTask setTaskDescription:kRetrieve];
    }
    [postDataTask resume];
    
}

#pragma mark - NSURLSession delegate

- (void)URLSession:(NSURLSession *)session dataTask:(NSURLSessionDataTask *)dataTask didReceiveData:(NSData *)data {
    NSMutableData *responseData = self.responsesData[@(dataTask.taskIdentifier)];
    if (!responseData) {
        responseData = [NSMutableData dataWithData:data];
        self.responsesData = [NSMutableDictionary dictionary];
        self.responsesData[@(dataTask.taskIdentifier)] = responseData;
    } else {
        [responseData appendData:data];
    }
}

- (void)URLSession:(NSURLSession *)session task:(NSURLSessionTask *)task didCompleteWithError:(NSError *)error {
    [_session finishTasksAndInvalidate];
    
    if (error) {
        _completionBlock(nil, 0, 0, error);
        return;
    }
    
    NSMutableData *responseData = self.responsesData[@(task.taskIdentifier)];
    
    if (responseData) {
        // parse message
        NSDictionary *dicResponse = [NSJSONSerialization JSONObjectWithData:responseData options:0 error:nil];
        NSString *message = nil;
        if (dicResponse) {
            if (!CheckNilAndNull(dicResponse)){
                if (!CheckNilAndNull(dicResponse[@"Messages"])){
                    message = dicResponse[@"Messages"];
                }
            }
            
        } else {
            NSLog(@"responseData = %@", [[NSString alloc] initWithData:responseData encoding:NSUTF8StringEncoding]);
        }
        
        [self.responsesData removeObjectForKey:@(task.taskIdentifier)];
        
        if ([task.taskDescription isEqualToString:kUpload]) {
            _completionBlock(message, 0, 0, error);
            return;
        }
        else{
            NSHTTPURLResponse *response = nil;
            response = (NSHTTPURLResponse *)task.response;
            
            NSDictionary *dicRoot = response.allHeaderFields;
            NSLog(@"StatusCode: %@, session description:%@",dicRoot, _session.description);
            
            if (!CheckNilAndNull(dicRoot)) {
                
                
                if (!CheckNilAndNull(dicRoot[@"Content-Range"])) {
                    NSString *strRange = dicRoot[@"Content-Range"];
                    
                    if([strRange rangeOfString:@"/"].location != NSNotFound){
                        NSString *strCurrent = [strRange componentsSeparatedByString:@"/"][0];
                        NSString *strTotal = [strRange componentsSeparatedByString:@"/"][1];
                        NSInteger current = [strCurrent integerValue];
                        NSInteger total = [strTotal integerValue];
                        _completionBlock(message, current, total, error);
                        return;
                    }
                    
                }
            }
            
        }
        
        
    } else {
        _completionBlock(nil, 0, 0, error);
    }
    
    
}

@end


#pragma mark - categoris

@implementation NSMutableArray (Queue)

- (id)dequeue {
    id headObject = [self objectAtIndex:0];
    if (headObject != nil) {
        [self removeObjectAtIndex:0];
    }
    return headObject;
}

- (void)enqueue:(id)anObject {
    [self addObject:anObject];
}

@end

//
// Mapping from 6 bit pattern to ASCII character.
//
static unsigned char base64EncodeLookup[65] =
"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

//
// Fundamental sizes of the binary and base64 encode/decode units in bytes
//
#define BINARY_UNIT_SIZE 3
#define BASE64_UNIT_SIZE 4

char *NewBase64Encode(
                      const void *buffer,
                      size_t length,
                      bool separateLines,
                      size_t *outputLength)
{
    const unsigned char *inputBuffer = (const unsigned char *)buffer;
    
#define MAX_NUM_PADDING_CHARS 2
#define OUTPUT_LINE_LENGTH 64
#define INPUT_LINE_LENGTH ((OUTPUT_LINE_LENGTH / BASE64_UNIT_SIZE) * BINARY_UNIT_SIZE)
#define CR_LF_SIZE 2
    
    //
    // Byte accurate calculation of final buffer size
    //
    size_t outputBufferSize =
    ((length / BINARY_UNIT_SIZE)
     + ((length % BINARY_UNIT_SIZE) ? 1 : 0))
    * BASE64_UNIT_SIZE;
    if (separateLines)
    {
        outputBufferSize +=
        (outputBufferSize / OUTPUT_LINE_LENGTH) * CR_LF_SIZE;
    }
    
    //
    // Include space for a terminating zero
    //
    outputBufferSize += 1;
    
    //
    // Allocate the output buffer
    //
    char *outputBuffer = (char *)malloc(outputBufferSize);
    if (!outputBuffer)
    {
        return NULL;
    }
    
    size_t i = 0;
    size_t j = 0;
    const size_t lineLength = separateLines ? INPUT_LINE_LENGTH : length;
    size_t lineEnd = lineLength;
    
    while (true)
    {
        if (lineEnd > length)
        {
            lineEnd = length;
        }
        
        for (; i + BINARY_UNIT_SIZE - 1 < lineEnd; i += BINARY_UNIT_SIZE)
        {
            //
            // Inner loop: turn 48 bytes into 64 base64 characters
            //
            outputBuffer[j++] = base64EncodeLookup[(inputBuffer[i] & 0xFC) >> 2];
            outputBuffer[j++] = base64EncodeLookup[((inputBuffer[i] & 0x03) << 4)
                                                   | ((inputBuffer[i + 1] & 0xF0) >> 4)];
            outputBuffer[j++] = base64EncodeLookup[((inputBuffer[i + 1] & 0x0F) << 2)
                                                   | ((inputBuffer[i + 2] & 0xC0) >> 6)];
            outputBuffer[j++] = base64EncodeLookup[inputBuffer[i + 2] & 0x3F];
        }
        
        if (lineEnd == length)
        {
            break;
        }
        
        //
        // Add the newline
        //
        outputBuffer[j++] = '\r';
        outputBuffer[j++] = '\n';
        lineEnd += lineLength;
    }
    
    if (i + 1 < length)
    {
        //
        // Handle the single '=' case
        //
        outputBuffer[j++] = base64EncodeLookup[(inputBuffer[i] & 0xFC) >> 2];
        outputBuffer[j++] = base64EncodeLookup[((inputBuffer[i] & 0x03) << 4)
                                               | ((inputBuffer[i + 1] & 0xF0) >> 4)];
        outputBuffer[j++] = base64EncodeLookup[(inputBuffer[i + 1] & 0x0F) << 2];
        outputBuffer[j++] =	'=';
    }
    else if (i < length)
    {
        //
        // Handle the double '=' case
        //
        outputBuffer[j++] = base64EncodeLookup[(inputBuffer[i] & 0xFC) >> 2];
        outputBuffer[j++] = base64EncodeLookup[(inputBuffer[i] & 0x03) << 4];
        outputBuffer[j++] = '=';
        outputBuffer[j++] = '=';
    }
    outputBuffer[j] = 0;
    
    //
    // Set the output length and return the buffer
    //
    if (outputLength)
    {
        *outputLength = j;
    }
    return outputBuffer;
}

@implementation NSData (Split)


- (NSMutableArray *)componentsSeparatedByChunkSize:(NSUInteger)pChunkSize {
    
    
    NSMutableArray *maBlocks = [NSMutableArray array];
    
    NSRange dataRange;
    
    NSUInteger chunksWritten = 0;
    for (chunksWritten = 0; chunksWritten * pChunkSize < [self length]; chunksWritten++) {
        
        dataRange = NSMakeRange(chunksWritten * pChunkSize, MIN(pChunkSize, [self length] - chunksWritten * pChunkSize));
        
        NSData *blockData = [self subdataWithRange:dataRange];
        
        
        [maBlocks addObject:blockData];
    }
    
    
    return maBlocks;
}


- (NSData *)componentsSeparatedFromByte:(NSInteger)sep
{
    NSRange startEndRange = NSMakeRange(sep, self.length - sep);
    NSData *line = [self subdataWithRange:startEndRange];
    
    return line;
}

- (NSString *)base64EncodedString
{
    size_t outputLength = 0;
    char *outputBuffer =
    NewBase64Encode([self bytes], [self length], true, &outputLength);
    
    NSString *result = [[NSString alloc] initWithBytes:outputBuffer
                                                length:outputLength
                                              encoding:NSASCIIStringEncoding];
    free(outputBuffer);
    return result;
}



@end
