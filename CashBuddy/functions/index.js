// imports firebase-functions module
const functions = require('firebase-functions');

// imports firebase-admin module
const admin = require('firebase-admin');

admin.initializeApp(functions.config().firebase);

//Notification for transfer
exports.transferNotification = functions.database.ref('/notifications/transfer/{user_id}/{notification_id}').onWrite(event =>{
    const user_id = event.params.user_id;
    const notification = event.params.notification;

    console.log('Sent notification to : ', user_id);

    if(!event.data.val()){
        return console.log('A Notification has been deleted from the database : ', notification_id);
    }

    const deviceToken = admin.database().ref(`/users/${user_id}/device_token`).once('value');

    return deviceToken.then(result =>{
        const token_id = result.val();

        const payload = {
            notification: {
                title : "Cash Buddy",
                body : "Yay! You just received CB Cash from a friend!",
                icon : "logonotif",
                sound: "default"
            }
        };

        return admin.messaging().sendToDevice(token_id, payload);
    });

});

//Notification for purchase
exports.purchaseNotification = functions.database.ref('/notifications/purchase/{user_id}/{notification_id}').onWrite(event =>{
    const user_id = event.params.user_id;
    const notification = event.params.notification;

    console.log('Sent notification to : ', user_id);

    if(!event.data.val()){
        return console.log('A Notification has been deleted from the database : ', notification_id);
    }

    const deviceToken = admin.database().ref(`/merchant/${user_id}/device_token`).once('value');

    return deviceToken.then(result =>{
        const token_id = result.val();

        const payload = {
            notification: {
                title : "Cash Buddy",
                body : "Yay! Successful transaction!",
                icon : "logonotif",
                sound: "default"
            }
        };

        return admin.messaging().sendToDevice(token_id, payload);
    });

});

//Notification for accept top up
exports.acceptTopUpNotification = functions.database.ref('/notifications/acceptTopUp/{user_id}/{notification_id}').onWrite(event =>{
    const user_id = event.params.user_id;
    const notification = event.params.notification;

    console.log('Sent notification to : ', user_id);

    if(!event.data.val()){
        return console.log('A Notification has been deleted from the database : ', notification_id);
    }

    const deviceToken = admin.database().ref(`/users/${user_id}/device_token`).once('value');

    return deviceToken.then(result =>{
        const token_id = result.val();

        const payload = {
            notification: {
                title : "Cash Buddy",
                body : "Successful top up!",
                icon : "logonotif",
                sound: "default"
            }
        };

        return admin.messaging().sendToDevice(token_id, payload);
    });

});

//Notification for withdraw
exports.withdrawNotification = functions.database.ref('/notifications/withdraw/{user_id}/{notification_id}').onWrite(event =>{
    const user_id = event.params.user_id;
    const notification = event.params.notification;

    console.log('Sent notification to : ', user_id);

    if(!event.data.val()){
        return console.log('A Notification has been deleted from the database : ', notification_id);
    }

    const deviceToken = admin.database().ref(`/merchant/${user_id}/device_token`).once('value');

    return deviceToken.then(result =>{
        const token_id = result.val();

        const payload = {
            notification: {
                title : "Cash Buddy",
                body : "Withdraw request has been processed",
                icon : "logonotif",
                sound: "default"
            }
        };

        return admin.messaging().sendToDevice(token_id, payload);
    });

});

//Notification for decline top up
exports.declineTopUpNotification = functions.database.ref('/notifications/declineTopUp/{user_id}/{notification_id}').onWrite(event =>{
    const user_id = event.params.user_id;
    const notification = event.params.notification;

    console.log('Sent notification to : ', user_id);

    if(!event.data.val()){
        return console.log('A Notification has been deleted from the database : ', notification_id);
    }

    const deviceToken = admin.database().ref(`/users/${user_id}/device_token`).once('value');

    return deviceToken.then(result =>{
        const token_id = result.val();

        const payload = {
            notification: {
                title : "Cash Buddy",
                body : "Oops! Top up request declined. Please try again.",
                icon : "logonotif",
                sound: "default"
            }
        };

        return admin.messaging().sendToDevice(token_id, payload);
    });

});