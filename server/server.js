var http = require("http");
var db = require("./mongodbOperator");

var server = http.createServer(function(request, res){
    console.log(request.method);
    switch(request.method) {
        case "POST":
            msg = "";
            request.setEncoding("utf8");
            request.on("end", function(){
                console.log(msg);
                var message = JSON.parse(msg);
                console.log(message);
                switch(message.task){
                    case "login":
                        console.log("login");
                        db.findOne({"username":message["username"], "password":message["password"]}, function(err, result) {
                            if(result !== null) {
                                res.setHeader("Content-Type", "application/json");
                                res.end(JSON.stringify(true));
                            } else {
                                res.setHeader("Content-Type", "application/json");
                                res.end(JSON.stringify(false));
                            }
                        });
                        break;
                    case "register":
                        console.log("register");
                        res.setHeader("Content-Type", "application/json");
                        db.findOne({"username":message["username"]}, function(err, result) {
                            if(err) {
                                res.statusCode = 500;
                                res.end(JSON.stringify(false));
                            }
                            if(result !== null) {
                                res.end(JSON.stringify(false));
                            } else {
                                db.insertOne({"username":message["username"], "password":message["password"]}, function(err, result){
                                    if(result === 1) {
                                        res.end(JSON.stringify(true));
                                    } else {
                                        res.end(JSON.stringify(false));
                                    }
                                });
                            }
                        });
                        break;
                    case "upload":
                        console.log("upload");
                        res.setHeader("Content-Type", "application/json");
                        db.findOne({"username":message["username"], "password":message["password"]}, function(err, result){
                            console.log("result", result);
                            if(result !== null) {
                                db.updateOne({"username":message["username"], "password":message["password"]}, {$set: {data: message["data"]}}, function(err, result) {
                                    if(result === 1) {
                                        res.end(JSON.stringify(true));
                                    } else {
                                        res.end(JSON.stringify(false));
                                    }
                                });
                            } else {
                                res.end(JSON.stringify(false));
                            }
                        });
                        break;
                    case "download":
                        console.log("download");
                        res.setHeader("Content-Type", "application/json");
                        db.findOne({"username":message["username"], "password":message["password"]}, function(err, result) {
                            if(result !== null) {
                                var message = JSON.parse(result.data);
                                console.log(message.toString());
                                message = "[" + message.toString() + "]";
                                res.setHeader("Content-Length", Buffer.byteLength(message.toString()));
                                res.end(message);
                            } else {
                                res.end(JSON.stringify({}));
                            }
                        }); 
                        break;
                    default:
                        break;
                }
            });
            request.on("data", function(chunk){
                msg += chunk;
            });
            request.on("close", function() {
                console.log("close", msg);
            });
            break;
        default:
    }
});
server.listen(8080);