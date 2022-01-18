import 'package:flutter/material.dart';
import 'dart:async';
import 'package:url_launcher/url_launcher.dart';

double textNormalSize = 15;
double textHeadingSize = 20;
String appName = "indra";

class PrivacySecurityScreen extends StatefulWidget {
  @override
  PrivacySecurityScreenState createState() => PrivacySecurityScreenState();
}

class PrivacySecurityScreenState extends State<PrivacySecurityScreen> {
  Future<void>? _launched;
  Future<void> _launchInBrowser() async {
    //var url = _mLaunchUri.toString();
    var url = "https://www.ephrine.in/privacy-policy.html";

    if (await canLaunch(url)) {
      await launch(
        url,
        forceSafariVC: false,
        forceWebView: false,
        //   headers: <String, String>{'my_header_key': 'my_header_value'},
      );
    } else {
      throw 'Could not launch $url';
    }
  }

  @override
  Widget build(BuildContext context) {
    var brightness = MediaQuery.of(context).platformBrightness;
    bool darkModeOn = brightness == Brightness.dark;

    Color dayNightTextColor = Colors.black;
    if (darkModeOn) {
      dayNightTextColor = Colors.white;
    } else {
      dayNightTextColor = Colors.black;
    }

    return Scaffold(
      appBar: AppBar(
        title: Text(
          "Privacy & Security",
          style: TextStyle(color: dayNightTextColor),
        ),
        elevation: 0.0,
        backgroundColor: Colors.transparent,
        automaticallyImplyLeading: false,
      ),
      body: ListView(
        children: <Widget>[
          Padding(
              padding: const EdgeInsets.all(15.0),
              child: Column(
                children: <Widget>[
                  Image(
                    image: AssetImage('images/security200px.png'),
                    height: 100,
                    width: 100,
                  ),
                  Text(
                    '\nYour calls remain private with end-to-end encryption',
                    textAlign: TextAlign.center,
                    style: TextStyle(
                        fontSize: textHeadingSize, fontWeight: FontWeight.bold),
                  ),
                  Text(
                    '\nTo keep your conversations private, $appName uses end-to-end encryption for calls.',
                    textAlign: TextAlign.left,
                    style: TextStyle(fontSize: textNormalSize),
                  ),
                  Text(" "),
                  Text(
                      '\n~ Only people in the call will know what\'s said or shown',
                      textAlign: TextAlign.left,
                      style: TextStyle(fontSize: textNormalSize)),
                  Text(
                      '\n~ $appName(and $appName team) can\'t see, hear or save your call\'s audio and video.',
                      textAlign: TextAlign.left,
                      style: TextStyle(fontSize: textNormalSize)),
                  Text(
                      '\n~ End-to-end encryption is a standard security method that protects communications data. It\'s built into every indra call, so you don\'t need to turn it on yourself and it can\'t be turned off.',
                      textAlign: TextAlign.left,
                      style: TextStyle(fontSize: textNormalSize)),
                  Text("\n\n"),
                  screenShotSection(context),
                  Text("\n\n"),
                  peer2peerSection(context),
                  Text("\n\n"),
                  ElevatedButton(
                    onPressed: () => setState(() {
                      _launched = _launchInBrowser();
                    }),
                    child: const Text('Read Privacy Policy'),
                    style: ElevatedButton.styleFrom(
                      primary: Colors.green,
                    ),
                  ),
                ],
              ))
        ],
      ),
    );
  }
}

@override
Widget screenShotSection(BuildContext context) {
  return Column(children: <Widget>[
    Image.asset(
      'images/screenshot-protection-hand-with-screen.png',
      width: 250,
    ),
    Text('Screenshot Protection',
        textAlign: TextAlign.left,
        style:
            TextStyle(fontSize: textHeadingSize, fontWeight: FontWeight.bold)),
    Text(
        '$appName has in-built feature to block screenshot & screen recording to protect user from bad actors and cyberbullying.',
        textAlign: TextAlign.left,
        style: TextStyle(fontSize: textNormalSize)),
  ]);
}

@override
Widget peer2peerSection(BuildContext context) {
  return Column(children: <Widget>[
    Image.asset(
      'images/peer2peer.png',
      width: 250,
    ),
    Text('Secure Peer-to-Peer Connection',
        textAlign: TextAlign.left,
        style:
            TextStyle(fontSize: textHeadingSize, fontWeight: FontWeight.bold)),
    Text(
        'Video & Audio Stream has end-to-end encrypted connection without any middle server to provide highest security',
        textAlign: TextAlign.left,
        style: TextStyle(fontSize: textNormalSize)),
  ]);
}
