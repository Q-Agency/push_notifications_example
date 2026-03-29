import 'dart:developer';

import 'package:firebase_core/firebase_core.dart';
import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import 'firebase_options.dart';

const String _channelName = 'agency.q.push_notifications_example';
const MethodChannel _nativeNotificationChannel = MethodChannel(_channelName);

final GlobalKey<NavigatorState> navigatorKey = GlobalKey<NavigatorState>();
final GlobalKey<ScaffoldMessengerState> scaffoldMessengerKey =
    GlobalKey<ScaffoldMessengerState>();

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await Firebase.initializeApp(options: DefaultFirebaseOptions.currentPlatform);

  FirebaseMessaging.instance.getToken().then((value) {
    if (kDebugMode) {
      log('FCM: $value');
    }
  });

  // Set presentation options for iOS/macOS when app is in the foreground
  await FirebaseMessaging.instance.setForegroundNotificationPresentationOptions(
    alert: true,
    badge: true,
    sound: true,
  );

  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      navigatorKey: navigatorKey,
      scaffoldMessengerKey: scaffoldMessengerKey,
      title: 'Flutter Demo',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.blue),
      ),
      home: const MyHomePage(title: 'Flutter Demo Home Page'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key, required this.title});
  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  @override
  void initState() {
    super.initState();

    _nativeNotificationChannel.setMethodCallHandler((call) async {
      if (call.method == 'onNotification') {
        final args = call.arguments;
        final Map<String, dynamic>? payload = (args is Map)
            ? Map<String, dynamic>.from(args)
            : null;
        final notification = _payload(payload);
        _handleNotificationAction(notification);
      }
    });

    _nativeNotificationChannel
        .invokeMethod('getInitialNotification')
        .then((result) {
          if (result != null) {
            final Map<String, dynamic>? payload = (result is Map)
                ? Map<String, dynamic>.from(result)
                : null;
            final display = _payload(payload);
            _handleNotificationAction(display);
          }
        })
        .catchError((e) {
          // Handle error
        });

    _nativeNotificationChannel.invokeMethod('dartReady');

    FirebaseMessaging.instance.getInitialMessage().then((
      RemoteMessage? message,
    ) {
      if (message != null) {
        final notification = message.notification;
        final body = notification?.body ?? message.data.toString();
        WidgetsBinding.instance.addPostFrameCallback((_) {
          final messenger = scaffoldMessengerKey.currentState;
          final navigator = navigatorKey.currentState;
          if (messenger != null) {
            messenger.showSnackBar(
              SnackBar(
                content: Text('Opened from terminated notification: $body'),
              ),
            );
          } else if (navigator != null) {
            showDialog<void>(
              context: navigator.overlay!.context,
              builder: (context) => AlertDialog(
                title: const Text('Notification firebase'),
                content: Text(body),
                actions: [
                  TextButton(
                    onPressed: () => Navigator.of(context).pop(),
                    child: const Text('OK'),
                  ),
                ],
              ),
            );
          } else {
            // Handle case where both messenger and navigator are null
          }
        });
      }
    });
  }

  String _payload(Map<String, dynamic>? payload) {
    if (payload == null) return '';
    final action = payload['action'];
    final reply = payload['reply'];
    if (action == null && reply == null) return payload.toString();
    final parts = <String>[];
    if (action != null) parts.add('action: $action');
    if (reply != null) parts.add('reply: $reply');
    return parts.join(', ');
  }

  void _handleNotificationAction(String body) {
    if (body.isEmpty) return;
    WidgetsBinding.instance.addPostFrameCallback((_) {
      final messenger = scaffoldMessengerKey.currentState;
      final navigator = navigatorKey.currentState;
      if (messenger != null) {
        messenger.showSnackBar(
          SnackBar(content: Text('Opened from native notification: $body')),
        );
      } else if (navigator != null) {
        showDialog<void>(
          context: navigator.overlay!.context,
          builder: (context) => AlertDialog(
            title: const Text('Notification'),
            content: Text(body),
            actions: [
              TextButton(
                onPressed: () => Navigator.of(context).pop(),
                child: const Text('OK'),
              ),
            ],
          ),
        );
      } else {
        // Handle case where both messenger and navigator are null
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
        title: Text(widget.title),
      ),
      body: Center(
        child: MaterialButton(
          color: Colors.blue,

          onPressed: () {
            FirebaseMessaging.instance.requestPermission(
              alert: true,
              sound: true,
              badge: true,
            );
          },
          child: Text('Request permissions!'),
        ),
      ),
    );
  }
}
