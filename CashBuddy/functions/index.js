// imports firebase-functions module
const functions = require('firebase-functions');

// imports firebase-admin module
const admin = require('firebase-admin');

admin.initializeApp(functions.config().firebase);

//Notification for transfer
exports.transferNotification = functions.database.ref('/notifications/transfer/{user_id}/{notification_id}').onWrite(event =>{
    const user_id = event.params.user_id;
    const notification = event.params.notification;

    console.log('Sent transfer notification to : ', user_id);

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

    console.log('Sent purchase notification to : ', user_id);

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

    console.log('Sent accepted top up notification to : ', user_id);

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

//Notification for accepted withdraw
exports.acceptWithdrawNotification = functions.database.ref('/notifications/acceptWithdraw/{type}/{user_id}/{notification_id}').onWrite(event =>{
    const user_id = event.params.user_id;
    const type = event.params.type
    const notification = event.params.notification;

    console.log('Sent accepted withdraw notification to : ', user_id);

    if(!event.data.val()){
        return console.log('A Notification has been deleted from the database : ', notification_id);
    }

    const deviceToken = admin.database().ref(`/${type}/${user_id}/device_token`).once('value');

    return deviceToken.then(result =>{
        const token_id = result.val();

        const payload = {
            notification: {
                title : "Cash Buddy",
                body : "Yay! Your withdraw request has been processed.",
                icon : "logonotif",
                sound: "default"
            }
        };

        return admin.messaging().sendToDevice(token_id, payload);
    });

});

//Notification for declined withdraw
exports.declineWithdrawNotification = functions.database.ref('/notifications/declineWithdraw/{type}/{user_id}/{notification_id}').onWrite(event =>{
    const user_id = event.params.user_id;
    const type = event.params.type;
    const notification = event.params.notification;

    console.log('Sent declined withdraw notification to : ', user_id);

    if(!event.data.val()){
        return console.log('A Notification has been deleted from the database : ', notification_id);
    }

    const deviceToken = admin.database().ref(`/${type}/${user_id}/device_token`).once('value');

    return deviceToken.then(result =>{
        const token_id = result.val();

        const payload = {
            notification: {
                title : "Cash Buddy",
                body : "Oops! Your withdrawal request has been rejected. Please try again!",
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

    console.log('Sent declined top up notification to : ', user_id);

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

//Notification for request payment
exports.requestPaymentNotification = functions.database.ref('/notifications/requestPayment/sent/{user_id}/{notification_id}').onWrite(event =>{
    const user_id = event.params.user_id;
    const notification = event.params.notification;

    console.log('Sent request payment notification to : ', user_id);

    if(!event.data.val()){
        return console.log('A Notification has been deleted from the database : ', notification_id);
    }

    const deviceToken = admin.database().ref(`/users/${user_id}/device_token`).once('value');

    return deviceToken.then(result =>{
        const token_id = result.val();

        const payload = {
            notification: {
                title : "Cash Buddy",
                body : "You received a payment request! Check it out now!",
                icon : "logonotif",
                sound: "default"
            }
        };

        return admin.messaging().sendToDevice(token_id, payload);
    });

});

//Notification for accepted request payment
exports.acceptRequestNotification = functions.database.ref('/notifications/requestPayment/accepted/{type}/{user_id}/{notification_id}').onWrite(event =>{
    const user_id = event.params.user_id;
    const type = event.params.type;
    const notification = event.params.notification;

    console.log('Sent accepted request notification to : ', user_id);

    if(!event.data.val()){
        return console.log('A Notification has been deleted from the database : ', notification_id);
    }

    const deviceToken = admin.database().ref(`/${type}/${user_id}/device_token`).once('value');

    return deviceToken.then(result =>{
        const token_id = result.val();

        const payload = {
            notification: {
                title : "Cash Buddy",
                body : "Yay! Your payment request is accepted!",
                icon : "logonotif",
                sound: "default"
            }
        };

        return admin.messaging().sendToDevice(token_id, payload);
    });

});

//Notification for rejeced request payment
exports.rejectRequestNotification = functions.database.ref('/notifications/requestPayment/rejected/{type}/{user_id}/{notification_id}').onWrite(event =>{
    const user_id = event.params.user_id;
    const type = event.params.type;
    const notification = event.params.notification;

    console.log('Sent rejected request notification to : ', user_id);

    if(!event.data.val()){
        return console.log('A Notification has been deleted from the database : ', notification_id);
    }

    const deviceToken = admin.database().ref(`/${type}/${user_id}/device_token`).once('value');

    return deviceToken.then(result =>{
        const token_id = result.val();

        const payload = {
            notification: {
                title : "Cash Buddy",
                body : "Oops! Your payment request was rejected!",
                icon : "logonotif",
                sound: "default"
            }
        };

        return admin.messaging().sendToDevice(token_id, payload);
    });

});