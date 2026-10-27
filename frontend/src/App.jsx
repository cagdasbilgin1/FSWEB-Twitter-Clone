import React, { useState, useEffect } from 'react';

const USERS = [
  { id: 1, name: 'Cagdas Bilgin' },
  { id: 3, name: 'Bowen Higgins' },
  { id: 4, name: 'Leighton Kramer' },
  { id: 5, name: 'Kylan Gentry' },
  { id: 6, name: 'Amelie Griffith' },
  { id: 7, name: 'Franklin Sierra' },
  { id: 8, name: 'Marceline Avila' },
  { id: 9, name: 'Jaylen Blackwell' },
  { id: 10, name: 'Saoirse Conrad' },
  { id: 11, name: 'Ester Wolf' },
  { id: 12, name: 'Jolene Fox' },
];

export default function App() {
  const [selectedUserId, setSelectedUserId] = useState(1);
  const [tweets, setTweets] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [newTweet, setNewTweet] = useState('');
  const [statusMessage, setStatusMessage] = useState('');

  const API_BASE = 'http://localhost:3000';

  const fetchTweets = async (userId) => {
    setLoading(true);
    setError(null);
    try {
      const response = await fetch(`${API_BASE}/tweet/findByUserId?userId=${userId}`);
      if (!response.ok) {
        throw new Error(`HTTP Hata: ${response.status} ${response.statusText}`);
      }
      const data = await response.json();
      setTweets(data);
    } catch (err) {
      console.error('Fetch error:', err);
      setError(
        err.message.includes('Failed to fetch') || err.message.includes('NetworkError')
          ? 'CORS veya Ağ Hatası: Backend (localhost:3000) çalışmıyor olabilir veya CORS izni verilmemiş olabilir.'
          : err.message
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTweets(selectedUserId);
  }, [selectedUserId]);

  const handleCreateTweet = async (e) => {
    e.preventDefault();
    if (!newTweet.trim()) return;

    setStatusMessage('');
    try {
      const response = await fetch(`${API_BASE}/tweet`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          content: newTweet,
          userId: Number(selectedUserId),
        }),
      });

      if (!response.ok) {
        throw new Error(`Tweet gönderilemedi: ${response.statusText}`);
      }

      setNewTweet('');
      setStatusMessage('Tweet başarıyla paylaşıldı!');
      fetchTweets(selectedUserId);
    } catch (err) {
      setError(err.message);
    }
  };

  const selectedUserName = USERS.find((u) => u.id === Number(selectedUserId))?.name || 'Kullanıcı';

  return (
    <div className="min-h-screen bg-gray-100 p-6 flex flex-col items-center">
      {/* User Selection */}
      <div className="w-full max-w-xl bg-white rounded-lg shadow-md p-4 mb-6">
        <div className="flex flex-col sm:flex-row sm:items-center gap-3">
          <label className="text-sm font-semibold text-gray-700 whitespace-nowrap">
            Aktif Kullanıcı Seç:
          </label>
          <select
            value={selectedUserId}
            onChange={(e) => setSelectedUserId(Number(e.target.value))}
            className="w-full sm:w-auto flex-1 border border-gray-300 rounded-md px-3 py-1.5 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-blue-400 font-medium text-gray-800"
          >
            {USERS.map((user) => (
              <option key={user.id} value={user.id}>
                {user.name} (ID: #{user.id})
              </option>
            ))}
          </select>
        </div>
      </div>

      {/* Tweet Oluşturma Formu */}
      <div className="w-full max-w-xl bg-white rounded-lg shadow-md p-6 mb-6">
        <div className="flex items-center gap-2 mb-3">
          <div className="w-7 h-7 rounded-full bg-blue-500 text-white font-bold flex items-center justify-center text-xs">
            {selectedUserName.charAt(0)}
          </div>
          <span className="text-sm font-semibold text-gray-800">
            {selectedUserName} olarak tweet at
          </span>
        </div>
        <form onSubmit={handleCreateTweet}>
          <textarea
            rows="3"
            value={newTweet}
            onChange={(e) => setNewTweet(e.target.value)}
            placeholder={`${selectedUserName}, neler düşünüyorsun?`}
            maxLength={280}
            className="w-full border border-gray-300 rounded-lg p-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-400 resize-none"
          />
          <div className="flex justify-between items-center mt-2">
            <span className="text-xs text-gray-400">
              {280 - newTweet.length} karakter kaldı
            </span>
            <button
              type="submit"
              disabled={!newTweet.trim()}
              className="bg-blue-500 hover:bg-blue-600 disabled:bg-gray-300 text-white font-semibold text-sm px-5 py-2 rounded-full transition"
            >
              Tweetle
            </button>
          </div>
        </form>
        {statusMessage && (
          <p className="mt-2 text-xs text-green-600 font-medium">{statusMessage}</p>
        )}
      </div>

      {/* Hata Mesajı */}
      {error && (
        <div className="w-full max-w-xl bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4 text-sm">
          <strong>Hata:</strong> {error}
        </div>
      )}

      {/* Tweet Listesi */}
      <div className="w-full max-w-xl space-y-4">
        <div className="flex justify-between items-center">
          <h2 className="text-lg font-semibold text-gray-800">
            {selectedUserName} Tweetleri ({tweets.length})
          </h2>
          <button
            onClick={() => fetchTweets(selectedUserId)}
            className="text-xs text-blue-500 hover:underline"
          >
            Yenile
          </button>
        </div>

        {loading ? (
          <div className="text-center py-8 text-gray-500 text-sm">Tweetler yükleniyor...</div>
        ) : tweets.length === 0 ? (
          <div className="bg-white rounded-lg shadow p-6 text-center text-gray-500 text-sm">
            Bu kullanıcıya ait henüz tweet bulunamadı. Yukarıdaki formdan ilk tweeti atabilirsiniz!
          </div>
        ) : (
          tweets.map((tweet) => (
            <div key={tweet.id} className="bg-white rounded-lg shadow p-5 border border-gray-100">
              <div className="flex justify-between items-start mb-2">
                <div className="flex items-center gap-2">
                  <div className="w-8 h-8 rounded-full bg-blue-500 text-white font-bold flex items-center justify-center text-xs">
                    {tweet.username ? tweet.username.charAt(0).toUpperCase() : 'U'}
                  </div>
                  <div>
                    <span className="font-semibold text-gray-800 text-sm">
                      {tweet.username || selectedUserName}
                    </span>
                    <span className="text-xs text-gray-400 ml-2">
                      {tweet.createdAt ? new Date(tweet.createdAt).toLocaleString('tr-TR') : ''}
                    </span>
                  </div>
                </div>
                <span className="text-xs text-gray-400">ID: #{tweet.id}</span>
              </div>

              <p className="text-gray-800 text-sm mt-2 mb-3 leading-relaxed whitespace-pre-wrap">
                {tweet.content}
              </p>

              <div className="flex items-center gap-6 text-xs text-gray-500 border-t pt-3">
                <span className="flex items-center gap-1 hover:text-red-500 cursor-pointer">
                  ❤️ {tweet.likeCount || 0} Beğeni
                </span>
                <span className="flex items-center gap-1 hover:text-green-500 cursor-pointer">
                  🔁 {tweet.retweetCount || 0} Retweet
                </span>
                <span className="flex items-center gap-1 hover:text-blue-500 cursor-pointer">
                  💬 {tweet.commentCount || 0} Yorum
                </span>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}
