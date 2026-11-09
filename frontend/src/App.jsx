import React, { useState, useEffect } from 'react';

export default function App() {
  const API_BASE = 'http://localhost:3000';

  // Route state ('/' or '/{username}')
  const [pathname, setPathname] = useState(() => window.location.pathname);
  const [profileUserId, setProfileUserId] = useState(null);

  // Auth state
  const [currentUser, setCurrentUser] = useState(() => {
    const saved = localStorage.getItem('twitter_user');
    return saved ? JSON.parse(saved) : null;
  });

  // Auth form states
  const [authTab, setAuthTab] = useState('login'); // 'login' | 'register'
  const [loginInput, setLoginInput] = useState('');
  const [loginPassword, setLoginPassword] = useState('');
  const [regUsername, setRegUsername] = useState('');
  const [regEmail, setRegEmail] = useState('');
  const [regPassword, setRegPassword] = useState('');
  const [authError, setAuthError] = useState(null);

  // Tweet feed states
  const [tweets, setTweets] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [newTweet, setNewTweet] = useState('');

  // Interaction states (Like, Retweet, Comment)
  const [userLikedMap, setUserLikedMap] = useState({}); // { [tweetId]: true }
  const [userRetweetMap, setUserRetweetMap] = useState({}); // { [tweetId]: retweetId }
  const [actionLoadingKey, setActionLoadingKey] = useState(null);
  const [openComments, setOpenComments] = useState({}); // { [tweetId]: boolean }
  const [commentInputs, setCommentInputs] = useState({}); // { [tweetId]: string }

  // Fetch current user's likes & retweets so active states and retweet IDs are synced
  const fetchUserInteractions = async (user) => {
    if (!user) {
      setUserLikedMap({});
      setUserRetweetMap({});
      return;
    }

    try {
      const [retweetsRes, likesRes] = await Promise.all([
        fetch(`${API_BASE}/retweet/user/${user.id}`),
        fetch(`${API_BASE}/like/user/${user.id}`),
      ]);

      if (retweetsRes.ok) {
        const retweetsData = await retweetsRes.json();
        const rtMap = {};
        retweetsData.forEach((rt) => {
          rtMap[rt.tweetId] = rt.id;
        });
        setUserRetweetMap(rtMap);
      }

      if (likesRes.ok) {
        const likesData = await likesRes.json();
        const lkMap = {};
        likesData.forEach((lk) => {
          lkMap[lk.tweetId] = true;
        });
        setUserLikedMap(lkMap);
      }
    } catch (err) {
      console.error('Interaction sync error:', err);
    }
  };

  const profileUsername = decodeURIComponent(pathname.replace(/^\/+|\/+$/g, ''));

  const navigateTo = (targetPath, userId = null) => {
    setProfileUserId(userId);
    if (window.location.pathname !== targetPath) {
      window.history.pushState({ userId }, '', targetPath);
      setPathname(targetPath);
    }
  };

  useEffect(() => {
    const handlePopState = (event) => {
      setPathname(window.location.pathname);
      setProfileUserId(event.state?.userId ?? null);
    };
    window.addEventListener('popstate', handlePopState);
    return () => window.removeEventListener('popstate', handlePopState);
  }, []);

  // Fetch tweets depending on current URL ('/' -> all tweets, '/{username}' -> user's tweets + retweets)
  const fetchTweets = async (silent = false) => {
    if (!silent) setLoading(true);
    setError(null);
    try {
      const response = await fetch(`${API_BASE}/tweet`);
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }
      const allTweets = await response.json();

      if (!profileUsername) {
        setTweets(allTweets);
      } else {
        const resolvedUserId =
          profileUserId ||
          (currentUser && currentUser.username === profileUsername ? currentUser.id : null) ||
          allTweets.find((t) => t.username === profileUsername)?.userId ||
          null;

        const retweetedIds = new Set();

        if (resolvedUserId) {
          try {
            const rtRes = await fetch(`${API_BASE}/retweet/user/${resolvedUserId}`);
            if (rtRes.ok) {
              const rtData = await rtRes.json();
              rtData.forEach((rt) => retweetedIds.add(rt.tweetId));
            }
          } catch (e) {
            console.error('Profile retweets fetch error:', e);
          }
        }

        const profileFeed = allTweets
          .filter((t) => {
            const isOwnTweet = t.username === profileUsername;
            const isRetweeted =
              retweetedIds.has(t.id) ||
              (resolvedUserId &&
                Array.isArray(t.retweetedByUserIds) &&
                t.retweetedByUserIds.includes(resolvedUserId));
            return isOwnTweet || isRetweeted;
          })
          .map((t) => {
            const isRetweeted =
              retweetedIds.has(t.id) ||
              (resolvedUserId &&
                Array.isArray(t.retweetedByUserIds) &&
                t.retweetedByUserIds.includes(resolvedUserId));

            if (t.username !== profileUsername && isRetweeted) {
              return { ...t, repostedBy: profileUsername };
            }
            return t;
          });

        setTweets(profileFeed);
      }
    } catch (err) {
      console.error('Fetch error:', err);
      setError('Tweetler yüklenirken bir hata oluştu veya sunucuya ulaşılamadı.');
    } finally {
      if (!silent) setLoading(false);
    }
  };

  useEffect(() => {
    fetchTweets();
  }, [pathname, currentUser]);

  useEffect(() => {
    fetchUserInteractions(currentUser);
  }, [currentUser]);

  // Login Handler
  const handleLogin = async (e) => {
    if (e) e.preventDefault();
    setAuthError(null);

    try {
      const response = await fetch(`${API_BASE}/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          username: loginInput.trim(),
          password: loginPassword,
        }),
      });

      const data = await response.json();
      if (!response.ok) {
        throw new Error(data.message || 'Giriş başarısız. Kullanıcı adı/email veya şifre hatalı.');
      }

      const userObj = {
        id: data.id,
        username: data.username,
        email: data.email,
        role: data.role,
      };

      setCurrentUser(userObj);
      localStorage.setItem('twitter_user', JSON.stringify(userObj));
      setLoginInput('');
      setLoginPassword('');
    } catch (err) {
      setAuthError(err.message);
    }
  };

  // Register Handler
  const handleRegister = async (e) => {
    e.preventDefault();
    setAuthError(null);

    if (!regPassword || regPassword.length < 6) {
      setAuthError('Şifre zorunludur ve en az 6 karakter olmalıdır.');
      return;
    }

    try {
      const response = await fetch(`${API_BASE}/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          username: regUsername.trim(),
          email: regEmail.trim(),
          password: regPassword,
        }),
      });

      const data = await response.json();
      if (!response.ok) {
        throw new Error(data.message || 'Kayıt başarısız oldu.');
      }

      setAuthTab('login');
      setRegUsername('');
      setRegEmail('');
      setRegPassword('');
    } catch (err) {
      setAuthError(err.message);
    }
  };

  // Logout Handler
  const handleLogout = () => {
    setCurrentUser(null);
    localStorage.removeItem('twitter_user');
    navigateTo('/');
  };

  // Create Tweet Handler
  const handleCreateTweet = async (e) => {
    e.preventDefault();
    if (!newTweet.trim() || !currentUser) return;

    try {
      const response = await fetch(`${API_BASE}/tweet`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          content: newTweet.trim(),
          userId: currentUser.id,
        }),
      });

      if (!response.ok) {
        throw new Error('Tweet paylaşılamadı.');
      }

      setNewTweet('');
      fetchTweets(true);
    } catch (err) {
      setError(err.message);
    }
  };

  // Helpers to check whether currentUser liked or retweeted a tweet
  const checkIsLiked = (tweet) => {
    if (!currentUser) return false;
    if (Array.isArray(tweet.likedByUserIds) && tweet.likedByUserIds.includes(currentUser.id)) {
      return true;
    }
    return Boolean(userLikedMap[tweet.id]);
  };

  const checkIsRetweeted = (tweet) => {
    if (!currentUser) return false;
    if (Array.isArray(tweet.retweetedByUserIds) && tweet.retweetedByUserIds.includes(currentUser.id)) {
      return true;
    }
    return Boolean(userRetweetMap[tweet.id]);
  };

  // Toggle Like / Dislike
  const handleToggleLike = async (tweet) => {
    if (!currentUser) {
      setError('Beğeni yapmak için lütfen önce giriş yapın.');
      return;
    }

    const alreadyLiked = checkIsLiked(tweet);
    const endpoint = alreadyLiked ? `${API_BASE}/dislike` : `${API_BASE}/like`;
    const key = `like-${tweet.id}`;
    setActionLoadingKey(key);
    setError(null);

    try {
      const response = await fetch(endpoint, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          tweetId: tweet.id,
          userId: currentUser.id,
        }),
      });

      if (!response.ok) {
        const errData = await response.json().catch(() => ({}));
        throw new Error(errData.message || 'Beğeni işlemi başarısız oldu.');
      }

      setUserLikedMap((prev) => {
        const next = { ...prev };
        if (alreadyLiked) {
          delete next[tweet.id];
        } else {
          next[tweet.id] = true;
        }
        return next;
      });

      await fetchTweets(true);
    } catch (err) {
      setError(err.message);
    } finally {
      setActionLoadingKey(null);
    }
  };

  // Toggle Retweet / Delete Retweet
  const handleToggleRetweet = async (tweet) => {
    if (!currentUser) {
      setError('Retweet yapmak için lütfen önce giriş yapın.');
      return;
    }

    if (tweet.userId === currentUser.id || tweet.username === currentUser.username) {
      return;
    }

    const alreadyRetweeted = checkIsRetweeted(tweet);
    const key = `retweet-${tweet.id}`;
    setActionLoadingKey(key);
    setError(null);

    try {
      if (alreadyRetweeted) {
        const retweetId = userRetweetMap[tweet.id];
        const deleteUrl = retweetId
          ? `${API_BASE}/retweet/${retweetId}?userId=${currentUser.id}`
          : `${API_BASE}/retweet/tweet/${tweet.id}?userId=${currentUser.id}`;

        const response = await fetch(deleteUrl, { method: 'DELETE' });
        if (!response.ok) {
          const errData = await response.json().catch(() => ({}));
          throw new Error(errData.message || 'Retweet geri alınamadı.');
        }

        setUserRetweetMap((prev) => {
          const next = { ...prev };
          delete next[tweet.id];
          return next;
        });
      } else {
        const response = await fetch(`${API_BASE}/retweet`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            tweetId: tweet.id,
            userId: currentUser.id,
          }),
        });

        const data = await response.json().catch(() => ({}));
        if (!response.ok) {
          throw new Error(data.message || 'Retweet yapılamadı.');
        }

        if (data.id) {
          setUserRetweetMap((prev) => ({ ...prev, [tweet.id]: data.id }));
        }
      }

      await fetchTweets(true);
    } catch (err) {
      setError(err.message);
    } finally {
      setActionLoadingKey(null);
    }
  };

  // Toggle Comments Drawer
  const handleToggleComments = (tweetId) => {
    setOpenComments((prev) => ({
      ...prev,
      [tweetId]: !prev[tweetId],
    }));
  };

  // Create Comment
  const handleCreateComment = async (e, tweetId) => {
    e.preventDefault();
    if (!currentUser) {
      setError('Yorum yapmak için lütfen önce giriş yapın.');
      return;
    }

    const content = (commentInputs[tweetId] || '').trim();
    if (!content) return;

    const key = `comment-add-${tweetId}`;
    setActionLoadingKey(key);
    setError(null);

    try {
      const response = await fetch(`${API_BASE}/comment`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          content,
          tweetId,
          userId: currentUser.id,
        }),
      });

      const data = await response.json().catch(() => ({}));
      if (!response.ok) {
        throw new Error(data.message || 'Yorum eklenemedi.');
      }

      setCommentInputs((prev) => ({ ...prev, [tweetId]: '' }));
      setOpenComments((prev) => ({ ...prev, [tweetId]: true }));
      await fetchTweets(true);
    } catch (err) {
      setError(err.message);
    } finally {
      setActionLoadingKey(null);
    }
  };

  // Delete Comment (Comment owner OR Tweet owner can delete)
  const handleDeleteComment = async (commentId) => {
    if (!currentUser) return;

    const key = `comment-del-${commentId}`;
    setActionLoadingKey(key);
    setError(null);

    try {
      const response = await fetch(`${API_BASE}/comment/${commentId}?userId=${currentUser.id}`, {
        method: 'DELETE',
      });

      if (!response.ok) {
        const errData = await response.json().catch(() => ({}));
        throw new Error(errData.message || 'Yorum silinemedi.');
      }

      await fetchTweets(true);
    } catch (err) {
      setError(err.message);
    } finally {
      setActionLoadingKey(null);
    }
  };

  return (
    <div className="min-h-screen bg-gray-100 flex flex-col items-center p-4 sm:p-6">
      {/* Top Navbar */}
      <header className="w-full max-w-xl bg-white rounded-xl shadow-sm p-4 mb-6 flex justify-between items-center border border-gray-200">
        <a
          href="/"
          onClick={(e) => {
            e.preventDefault();
            navigateTo('/');
          }}
          className="flex items-center gap-2 cursor-pointer"
        >
          <span className="text-2xl">🐦</span>
          <span className="font-bold text-xl text-blue-500 tracking-tight">Twitter</span>
        </a>

        {currentUser && (
          <div className="flex items-center gap-3">
            <a
              href={`/${encodeURIComponent(currentUser.username)}`}
              onClick={(e) => {
                e.preventDefault();
                navigateTo(`/${encodeURIComponent(currentUser.username)}`, currentUser.id);
              }}
              className="flex items-center gap-2 cursor-pointer hover:opacity-80 transition"
            >
              <div className="w-8 h-8 rounded-full bg-blue-500 text-white font-bold flex items-center justify-center text-xs shadow-inner">
                {currentUser.username.charAt(0).toUpperCase()}
              </div>
              <div className="text-left">
                <p className="text-xs font-bold text-gray-800 leading-tight">
                  {currentUser.username}
                </p>
                <p className="text-[11px] text-gray-500 hidden sm:block">{currentUser.email}</p>
              </div>
            </a>
            <button
              onClick={handleLogout}
              className="text-xs font-semibold text-red-500 hover:text-red-700 bg-red-50 hover:bg-red-100 px-3 py-1.5 rounded-lg transition cursor-pointer"
            >
              Çıkış
            </button>
          </div>
        )}
      </header>

      {/* Auth Card (if not logged in) */}
      {!currentUser && (
        <div className="w-full max-w-xl bg-white rounded-xl shadow-md p-6 mb-6 border border-gray-200">
          {/* Tabs */}
          <div className="flex border-b border-gray-200 mb-5">
            <button
              onClick={() => { setAuthTab('login'); setAuthError(null); }}
              className={`flex-1 py-2.5 text-sm font-semibold transition border-b-2 ${authTab === 'login'
                ? 'border-blue-500 text-blue-600'
                : 'border-transparent text-gray-500 hover:text-gray-700'
                }`}
            >
              Giriş Yap
            </button>
            <button
              onClick={() => { setAuthTab('register'); setAuthError(null); }}
              className={`flex-1 py-2.5 text-sm font-semibold transition border-b-2 ${authTab === 'register'
                ? 'border-blue-500 text-blue-600'
                : 'border-transparent text-gray-500 hover:text-gray-700'
                }`}
            >
              Kayıt Ol
            </button>
          </div>

          {authError && (
            <div className="bg-red-50 border border-red-300 text-red-700 text-xs p-3 rounded-lg mb-4">
              {authError}
            </div>
          )}

          {/* Login Form */}
          {authTab === 'login' ? (
            <div>
              <form onSubmit={handleLogin} className="space-y-3">
                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">
                    Kullanıcı Adı veya E-Posta
                  </label>
                  <input
                    type="text"
                    required
                    value={loginInput}
                    onChange={(e) => setLoginInput(e.target.value)}
                    placeholder="E-Postanızı giriniz"
                    className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-400"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">
                    Şifre
                  </label>
                  <input
                    type="password"
                    required
                    value={loginPassword}
                    onChange={(e) => setLoginPassword(e.target.value)}
                    placeholder="Şifrenizi giriniz"
                    className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-400"
                  />
                </div>
                <button
                  type="submit"
                  className="w-full bg-blue-500 hover:bg-blue-600 text-white text-sm font-semibold py-2.5 rounded-lg transition shadow-sm"
                >
                  Giriş Yap
                </button>
              </form>
            </div>
          ) : (
            /* Register Form */
            <form onSubmit={handleRegister} className="space-y-3">
              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">
                  Kullanıcı Adı
                </label>
                <input
                  type="text"
                  required
                  value={regUsername}
                  onChange={(e) => setRegUsername(e.target.value)}
                  placeholder="Kullanıcı Adı"
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-400"
                />
              </div>
              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">
                  E-Posta
                </label>
                <input
                  type="email"
                  required
                  value={regEmail}
                  onChange={(e) => setRegEmail(e.target.value)}
                  placeholder="E-Posta"
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-400"
                />
              </div>
              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">
                  Şifre
                </label>
                <input
                  type="password"
                  required
                  minLength={6}
                  value={regPassword}
                  onChange={(e) => setRegPassword(e.target.value)}
                  placeholder="Şifre"
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-400"
                />
              </div>
              <button
                type="submit"
                className="w-full bg-blue-500 hover:bg-blue-600 text-white text-sm font-semibold py-2.5 rounded-lg transition shadow-sm"
              >
                Kayıt Ol
              </button>
            </form>
          )}
        </div>
      )}

      {/* Tweet Oluşturma Formu (Only for logged-in user) */}
      {currentUser && (
        <div className="w-full max-w-xl bg-white rounded-xl shadow-sm p-5 mb-6 border border-gray-200">
          <form onSubmit={handleCreateTweet}>
            <textarea
              rows="3"
              value={newTweet}
              onChange={(e) => setNewTweet(e.target.value)}
              placeholder="Tweet yaz..."
              maxLength={280}
              className="w-full border border-gray-300 rounded-lg p-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-400 resize-none"
            />
            <div className="flex justify-end mt-2">
              <button
                type="submit"
                disabled={!newTweet.trim()}
                className="bg-blue-500 hover:bg-blue-600 disabled:bg-gray-300 text-white font-semibold text-sm px-5 py-2 rounded-full transition shadow-sm"
              >
                Tweetle
              </button>
            </div>
          </form>
        </div>
      )}

      {/* Error Message */}
      {error && (
        <div className="w-full max-w-xl bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded-lg mb-4 text-sm">
          <strong>Hata:</strong> {error}
        </div>
      )}

      {/* Tweet Feed */}
      <div className="w-full max-w-xl space-y-4">
        {loading ? (
          <div className="text-center py-8 text-gray-500 text-sm">Tweetler yükleniyor...</div>
        ) : tweets.length === 0 ? (
          <div className="bg-white rounded-xl shadow-sm p-8 text-center text-gray-500 text-sm border border-gray-200">
            Henüz tweet bulunamadı.
          </div>
        ) : (
          tweets.map((tweet) => {
            const isLiked = checkIsLiked(tweet);
            const isRetweeted = checkIsRetweeted(tweet);
            const isOwnTweet = Boolean(
              currentUser &&
                (tweet.userId === currentUser.id || tweet.username === currentUser.username)
            );
            const isCommentsOpen = Boolean(openComments[tweet.id]);
            const comments = Array.isArray(tweet.comments) ? tweet.comments : [];

            return (
              <div key={tweet.id} className="bg-white rounded-xl shadow-sm p-5 border border-gray-200 hover:shadow-md transition">
                {tweet.repostedBy && (
                  <div className="flex items-center gap-1.5 text-xs text-gray-500 font-medium mb-2">
                    <span>🔁</span>
                    <span>@{tweet.repostedBy} reposted</span>
                  </div>
                )}
                <div className="flex justify-between items-start mb-2">
                  <div className="flex items-center gap-2.5">
                    <a
                      href={`/${encodeURIComponent(tweet.username)}`}
                      onClick={(e) => {
                        e.preventDefault();
                        navigateTo(`/${encodeURIComponent(tweet.username)}`, tweet.userId);
                      }}
                      className="flex items-center gap-2.5 cursor-pointer hover:opacity-80 transition"
                    >
                      <div className="w-9 h-9 rounded-full bg-blue-500 text-white font-bold flex items-center justify-center text-xs shadow-inner">
                        {tweet.username ? tweet.username.charAt(0).toUpperCase() : 'U'}
                      </div>
                      <span className="font-semibold text-gray-900 text-sm">
                        {tweet.username}
                      </span>
                    </a>
                    <span className="text-[11px] text-gray-400">
                      {tweet.createdAt ? new Date(tweet.createdAt).toLocaleString('tr-TR') : ''}
                    </span>
                  </div>
                  <span className="text-[11px] text-gray-400">#{tweet.id}</span>
                </div>

                <p className="text-gray-800 text-sm mt-2 mb-3 leading-relaxed whitespace-pre-wrap">
                  {tweet.content}
                </p>

                {/* Action Bar: Like, Retweet, Comment */}
                <div className="flex items-center gap-3 text-xs border-t border-gray-100 pt-3">
                  <button
                    type="button"
                    disabled={actionLoadingKey === `like-${tweet.id}`}
                    onClick={() => handleToggleLike(tweet)}
                    className={`flex items-center gap-1.5 px-2.5 py-1.5 rounded-full transition cursor-pointer ${isLiked
                      ? 'text-red-600 bg-red-50 font-semibold'
                      : 'text-gray-500 hover:text-red-500'
                      }`}
                  >
                    <span>❤️</span>
                    <span>{tweet.likeCount || 0}</span>
                  </button>

                  {!isOwnTweet && (
                    <button
                      type="button"
                      disabled={actionLoadingKey === `retweet-${tweet.id}`}
                      onClick={() => handleToggleRetweet(tweet)}
                      className={`flex items-center gap-1.5 px-2.5 py-1.5 rounded-full transition cursor-pointer ${isRetweeted
                        ? 'text-green-600 bg-green-50 font-semibold'
                        : 'text-gray-500 hover:text-green-600'
                        }`}
                    >
                      <span>🔁</span>
                      <span>{tweet.retweetCount || 0}</span>
                    </button>
                  )}

                  <button
                    type="button"
                    onClick={() => handleToggleComments(tweet.id)}
                    className={`flex items-center gap-1.5 px-2.5 py-1.5 rounded-full transition cursor-pointer ${isCommentsOpen
                      ? 'text-blue-600 bg-blue-50 font-semibold'
                      : 'text-gray-500 hover:text-blue-500'
                      }`}
                  >
                    <span>💬</span>
                    <span>{tweet.commentCount ?? comments.length}</span>
                  </button>
                </div>

                {/* Comments Section */}
                {isCommentsOpen && (
                  <div className="mt-3 pt-3 border-t border-gray-100 space-y-2.5">
                    {comments.length > 0 && (
                      <div className="space-y-2">
                        {comments.map((comment) => {
                          const isCommentOwner = currentUser && currentUser.id === comment.userId;
                          const isTweetOwner = currentUser && currentUser.id === tweet.userId;
                          const canDelete = isCommentOwner || isTweetOwner;

                          return (
                            <div
                              key={comment.id}
                              className="bg-gray-50 rounded-lg p-2.5 border border-gray-100 text-xs"
                            >
                              <div className="flex justify-between items-center mb-1">
                                <a
                                  href={`/${encodeURIComponent(comment.username)}`}
                                  onClick={(e) => {
                                    e.preventDefault();
                                    navigateTo(`/${encodeURIComponent(comment.username)}`, comment.userId);
                                  }}
                                  className="font-semibold text-gray-800 cursor-pointer hover:opacity-80 transition"
                                >
                                  {comment.username}
                                </a>

                                {canDelete && (
                                  <button
                                    type="button"
                                    disabled={actionLoadingKey === `comment-del-${comment.id}`}
                                    onClick={() => handleDeleteComment(comment.id)}
                                    className="text-[11px] text-red-500 hover:text-red-700 cursor-pointer"
                                  >
                                    Sil
                                  </button>
                                )}
                              </div>

                              <p className="text-gray-700 whitespace-pre-wrap">
                                {comment.content}
                              </p>
                            </div>
                          );
                        })}
                      </div>
                    )}

                    {currentUser && (
                      <form
                        onSubmit={(e) => handleCreateComment(e, tweet.id)}
                        className="flex gap-2"
                      >
                        <input
                          type="text"
                          maxLength={280}
                          value={commentInputs[tweet.id] || ''}
                          onChange={(e) =>
                            setCommentInputs((prev) => ({
                              ...prev,
                              [tweet.id]: e.target.value,
                            }))
                          }
                          placeholder="Yorum yaz..."
                          className="flex-1 border border-gray-300 rounded-lg px-3 py-1.5 text-xs focus:outline-none bg-white"
                        />
                        <button
                          type="submit"
                          disabled={
                            !(commentInputs[tweet.id] || '').trim() ||
                            actionLoadingKey === `comment-add-${tweet.id}`
                          }
                          className="bg-blue-500 hover:bg-blue-600 disabled:bg-gray-300 text-white text-xs font-semibold px-3.5 py-1.5 rounded-lg cursor-pointer"
                        >
                          Gönder
                        </button>
                      </form>
                    )}
                  </div>
                )}
              </div>
            );
          })
        )}
      </div>
    </div>
  );
}
