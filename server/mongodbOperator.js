var MongoClient = require("mongodb").MongoClient;

const url = "mongodb://localhost:27017";
const dbName = "timeperiod";

const findOne = function(selector, callback){
    MongoClient.connect(url, function(err, client){
        if(err) {
            return callback(err);
        }
        const db = client.db(dbName);
        const collection = db.collection("test");
        collection.findOne(selector, function(err, result){
            client.close();
            console.log(result);
            return callback(null, result);
        });
    });
}

//findOne({"username":"abc", "password":"abc"},function(err, result){
//    console.log(result.data);
//    console.log(JSON.parse(result.data).toString());
//});

const updateOne = function(selector, obj, callback){
    MongoClient.connect(url, function(err, client){
        if(err) {
            return callback(err);
        }
        const db = client.db(dbName);
        const collection = db.collection("test");
        collection.updateOne(selector, obj, function(err, result){
            console.log(result.modifiedCount);
            client.close();
            return callback(null, result.modifiedCount);
        });
    });
}

//updateOne({"username":"username"}, {$set:{abc:[123,134]}}, function(err, result){
//});

const insertOne = function(obj, callback) {
    MongoClient.connect(url, function(err, client) {
        if(err) {
            return callback(err);
        }
        const db = client.db(dbName);
        const collection = db.collection("test");
        collection.insertOne(obj, function(err, result){
            client.close();
            console.log(result.insertedCount);
            return callback(null, result.insertedCount);
        });
    });
}
//insertOne({'username':"abc", "password":"abc"}, function(err, result){
//    console.log(result);
//});

exports.insertOne = insertOne;
exports.updateOne = updateOne;
exports.findOne = findOne;
