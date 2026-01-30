from flask import Flask, jsonify, request
import subprocess
import json

app = Flask(__name__)

@app.route('/health', methods=['GET'])
def health():
    return jsonify({'status': 'ok'})

@app.route('/version', methods=['GET'])
def version():
    try:
        result = subprocess.run(['yt-dlp', '--version'], capture_output=True, text=True, timeout=10)
        return jsonify({'version': result.stdout.strip()})
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@app.route('/live-streams', methods=['GET'])
def live_streams():
    channel_url = request.args.get('url', 'https://www.youtube.com/@LofiGirl/streams')

    try:
        result = subprocess.run([
            'yt-dlp',
            '-j',
            '--flat-playlist',
            '--extractor-args', 'youtube:player_client=web',
            channel_url
        ], capture_output=True, text=True, timeout=120)

        if result.returncode != 0:
            return jsonify({'error': result.stderr, 'tracks': []}), 500

        tracks = []
        track_id = 1

        for line in result.stdout.strip().split('\n'):
            if not line:
                continue
            try:
                data = json.loads(line)
                live_status = data.get('live_status')

                if live_status == 'is_live':
                    video_id = data.get('id')
                    title = data.get('title', 'Unknown')
                    url = data.get('url') or f"https://www.youtube.com/watch?v={video_id}"

                    if video_id and url:
                        tracks.append({
                            'id': track_id,
                            'videoId': video_id,
                            'titleName': title,
                            'url': url
                        })
                        track_id += 1
            except json.JSONDecodeError:
                continue

        return jsonify({'tracks': tracks})

    except subprocess.TimeoutExpired:
        return jsonify({'error': 'Request timed out', 'tracks': []}), 504
    except Exception as e:
        return jsonify({'error': str(e), 'tracks': []}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8080)
