

async function uploadVideo(videoFile, uploadChunksRetryCount, chunkSize, progressBar) {

    const bytes = await fileToByteArray(videoFile);
    const totalSize = bytes.length;
    const chunks = Math.ceil(totalSize / chunkSize);
    const lastChunkSize = totalSize % chunkSize;

    //send create video //step 1
    let fileId = await getFileUploadId(totalSize, chunks);
    if(fileId === -1){
        return -1;
    }


    //send chunks //step 2
    let nums = [...Array(chunks).keys()];
    let error = true;
    for(let i = 0; i < uploadChunksRetryCount; i++) {
        nums = await sendChunks(nums, chunks, bytes, chunkSize, progressBar, fileId);
        if(nums.length === 0) {

            //finalize video upload //step 3
            let vidId = await sendFinalizeVideo(chunks, fileId);
            if(vidId[0]){
                progressBar.style.width = "100%";
                return vidId[1];
            }
            else{
                nums = vidId[1];
            }
        }
        console.log("retry send chunks...")
    }

    return -1;

}


//step 1
async function getFileUploadId(totalsize, chunkCount){

    let data = { totalSize: totalsize, chunkCount: chunkCount };

    const response = await fetch("/uploadVideo", {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    });

    console.log("step 1");

    if(response.ok){
        return await response.text();
    }
    else{
        //TODO Error
    }

    return -1;
}


//step 2
async function sendChunks(nums, chunkCount, bytes, chunkSize, progressBar, fileId){

    let promises = [];
    const lastChunkSize = bytes.length % chunkSize;

    for(let i = 0; i < nums.length; i++){

        //create promise
        let p = new Promise(async function (resolve, reject) {

            let b = bytes.slice(chunkSize * nums.at(i), (chunkSize * (nums.at(i) + 1)));

            //handle last chunk's different size
            if(nums.at(i) === chunkCount - 1){
                b = bytes.slice(chunkSize * (chunkCount - 1), (chunkSize * (chunkCount - 1)) + lastChunkSize);
            }

            let outcome = await uploadChunk(nums.at(i), chunkCount, b, fileId);
            if (outcome[0]) {
                resolve(outcome[1]);
            } else {
                reject(outcome[1]);
            }
        });
        promises.push(p);

        if(i % 100 === 0){
            await Promise.allSettled(promises);
            console.log(((i / chunkCount) * 100) + "%");
            progressBar.style.width = ((i / chunkCount) * 100) + "%";
        }

    }

    //see if any errors
    await Promise.allSettled(promises);
    let errorNums = [];
    for(let i = 0; i < promises.length; i++){
        promises[i].then(
            function(val){ //success

            },
            function(val){ //error
                console.log("error " + val);
                errorNums.push(val);
            }
        );
    }

    return errorNums
}


async function uploadChunk(i, totalChunks, bytes, fileid){

    const controller = new AbortController();
    const timeoutId = setTimeout(() => {
        console.log(`[uploadChunk] Timeout for chunk ${i}`);
        controller.abort();
    }, 3000);

    let response = null;

    try {

        response = await fetch("/uploadVideo/" + fileid, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/octet-stream',
                'x-chunk-number': i,
                'x-total-chunk-count': totalChunks
            },
            body: bytes,
            signal: controller.signal
        });

        const responseText = await response.text();
        if (response.ok) {
            clearTimeout(timeoutId);

            return [true, i];
        }

        return [false, i];
    }
    catch (error){
        clearTimeout(timeoutId);
        return[false, i];
    }

}


//step 3
async function sendFinalizeVideo(chunkCount, fileid){

    //let data = { movie: movieId};

    const response = await fetch("/uploadVideo/" + fileid + "/finish/", {
        method: 'POST',
        headers: { 'Content-Type': 'application/json',
            'x-total-chunk-count': chunkCount},
        //body: JSON.stringify(data)
    });

    console.log("step 3");
    console.log(response);

    let content = await response.text();
    if(response.ok){
        return [true, content];
    }
    else{
        return [false, []]
        //TODO  get needed nums
    }

}


function fileToByteArray(file) {
    return new Promise((resolve, reject) => {
        var reader = new FileReader();
        reader.readAsArrayBuffer(file);

        reader.onloadend = function (evt) {
            if (evt.target.readyState === FileReader.DONE) {
                var arrayBuffer = evt.target.result;
                var uint8Array = new Uint8Array(arrayBuffer);
                resolve(uint8Array); // Return Uint8Array, not Array.from()
            }
        };

        reader.onerror = function (evt) {
            reject(evt.target.error);
        };
    });
}