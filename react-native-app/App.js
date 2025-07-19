import { StatusBar } from 'expo-status-bar';
import { StyleSheet, Text, View, TouchableOpacity } from 'react-native';
import WebView from 'react-native-webview';
import { YoutubePlayer } from 'react-native-youtube-iframe';
import { Ionicons } from '@expo/vector-icons';
import { useEffect, useRef, useState } from 'react';
import { useWindowDimensions } from 'react-native';

export default function App() {
  const [activeVideoId, setActiveVideoId] = useState(null);
  const [isManuallyPaused, setIsManuallyPaused] = useState(false);
  const refs = useRef({});
  const { width: screenWidth, height: screenHeight } = useWindowDimensions();

  const handleVideoTap = (videoId) => {
    setActiveVideoId(videoId);
    setIsManuallyPaused(false);
    if (refs.current[videoId]) {
      refs.current[videoId].playVideo();
    }
  };

  const handlePause = () => {
    setIsManuallyPaused(true);
    if (activeVideoId && refs.current[activeVideoId]) {
      refs.current[activeVideoId].pauseVideo();
    }
  };

  useEffect(() => {
    const interval = setInterval(() => {
      if (activeVideoId && refs.current[activeVideoId]) {
        refs.current[activeVideoId].seekTo(0);
        refs.current[activeVideoId].playVideo();
      }
    }, 1000); // Seek to 0 and play every second
    return () => clearInterval(interval);
  }, [activeVideoId]);

  // Updated shortsData with types
  const shortsData = [
    {
      type: 'youtube',
      videoId: '--ZyoAKTl1w',
      title: 'Zakup biletu jednorazowego w systemie FALA w MZK Chojnice',
      channelTitle: '@MZKChojnice53',
      likeCount: 6,
      publishDate: 'Mar 8, 2025',
    },
    {
      type: 'youtube',
      videoId: '-0yoC8Sq28s',
      title: 'FRANCEPIERRE PARIS, pierre naturelle grise du Périgord. au 36 rue de Bourgogne à Paris.',
      channelTitle: '@francepierre4573',
      likeCount: 31,
      publishDate: 'Feb 11, 2023',
    },
    {
      type: 'tiktok',
      videoId: '1234567890123456789',  // Sample TikTok ID
    },
    {
      type: 'youtube',
      videoId: '-1gY1d4NMqs',
      title: 'Short promo The melody of now',
      channelTitle: '@anettaaleksandra',
      likeCount: 0,
      publishDate: 'Feb 23, 2024',
    },
  ];

  const renderShort = ({ item, index }) => {
    const isActive = activeVideoId === item.videoId;
    return (
      <View style={styles.videoContainer}>
        {item.type === 'youtube' ? (
          <YoutubePlayer
            ref={ref => (refs.current[item.videoId] = ref)}
            height={screenHeight}
            width={screenWidth}
            videoId={item.videoId}
            play={isActive && !isManuallyPaused}
            initialPlayerParams={{
              autoplay: 1,
              mute: 1,
              controls: 0,
              modestbranding: 1,
              fs: 0,
              rel: 0,
              showinfo: 0,
              iv_load_policy: 3,
              cc_load_policy: 0,
              playsinline: 1,
              loop: 0,
              disablekb: 1,
              enablejsapi: 1,
              origin: 'https://www.youtube.com',  // Add for security/autoplay
            }}
            onError={(error) => console.log('YouTube Error:', error)}  // Add logging
          />
        ) : (
          <WebView
            source={{ uri: `https://www.tiktok.com/embed/v2/${item.videoId}` }}
            style={{ height: screenHeight, width: screenWidth }}
            allowsInlineMediaPlayback={true}
            mediaPlaybackRequiresUserAction={false}
            allowsFullscreenVideo={false}
            javaScriptEnabled={true}
            originWhitelist={['*']}
            onError={(error) => console.log('TikTok Error:', error)}  // Add logging
          />
        )}
        <TouchableOpacity style={styles.tapArea} onPress={() => handleVideoTap(item.videoId)} />
        {isManuallyPaused && isActive && (
          <View style={styles.pauseIndicator}>
            <Ionicons name="play" size={60} color="rgba(255,255,255,0.8)" />
          </View>
        )}
        {/* Removed all overlays */}
      </View>
    );
  };

  return (
    <View style={styles.container}>
      <Text>Open up App.js to start working on your app!</Text>
      <StatusBar style="auto" />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
    alignItems: 'center',
    justifyContent: 'center',
  },
  videoContainer: {
    position: 'relative',
    width: '100%',
    height: '100%',
    backgroundColor: '#000',
  },
  tapArea: {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
  },
  pauseIndicator: {
    position: 'absolute',
    top: '50%',
    left: '50%',
    transform: [{ translateX: -30 }, { translateY: -30 }],
    zIndex: 10,
  },
});
