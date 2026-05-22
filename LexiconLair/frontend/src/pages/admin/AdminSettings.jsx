import { useEffect, useState } from 'react';
import Layout from '../../components/Layout';
import { getAdminSettings, updateAdminSettings } from '../../api/client';

export default function AdminSettings() {
  const [settings, setSettings] = useState({
    externalApiDelayMs: 50,
    externalApiBatchSize: 10,
    gameOptionCount: 4,
    gameQuestionCount: 10,
  });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');

  useEffect(() => {
    getAdminSettings()
      .then(setSettings)
      .catch(err => setError(err.message || 'Unable to load admin settings.'))
      .finally(() => setLoading(false));
  }, []);

  function updateField(field, value) {
    setSettings(prev => ({ ...prev, [field]: value }));
    setMessage('');
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    setMessage('');
    setSaving(true);

    const payload = {
      externalApiDelayMs: Math.max(0, Number(settings.externalApiDelayMs) || 0),
      externalApiBatchSize: Math.max(1, Number(settings.externalApiBatchSize) || 1),
      gameOptionCount: Math.max(2, Number(settings.gameOptionCount) || 2),
      gameQuestionCount: Math.max(1, Number(settings.gameQuestionCount) || 1),
    };

    try {
      const saved = await updateAdminSettings(payload);
      setSettings(saved);
      setMessage('Settings saved.');
    } catch (err) {
      setError(err.message || 'Unable to save admin settings.');
    } finally {
      setSaving(false);
    }
  }

  return (
    <Layout>
      <div className="container">
        <h1>Admin Settings</h1>

        {error && <div className="alert alert-danger">{error}</div>}
        {message && <div className="alert alert-success">{message}</div>}

        {loading ? (
          <div className="alert alert-info">Loading settings...</div>
        ) : (
          <form onSubmit={handleSubmit} className="card">
            <div className="card-body">
              <div className="mb-3">
                <label htmlFor="externalApiBatchSize" className="form-label">
                  External API batch size
                </label>
                <input
                  id="externalApiBatchSize"
                  type="number"
                  min="1"
                  className="form-control"
                  value={settings.externalApiBatchSize}
                  onChange={e => updateField('externalApiBatchSize', e.target.value)}
                  required
                />
                <div className="form-text">Number of dictionary calls to make before pausing.</div>
              </div>

              <div className="mb-3">
                <label htmlFor="externalApiDelayMs" className="form-label">
                  External API delay in milliseconds
                </label>
                <input
                  id="externalApiDelayMs"
                  type="number"
                  min="0"
                  className="form-control"
                  value={settings.externalApiDelayMs}
                  onChange={e => updateField('externalApiDelayMs', e.target.value)}
                  required
                />
                <div className="form-text">Pause duration after each batch.</div>
              </div>

              <div className="mb-3">
                <label htmlFor="gameOptionCount" className="form-label">
                  Game option count
                </label>
                <input
                  id="gameOptionCount"
                  type="number"
                  min="2"
                  className="form-control"
                  value={settings.gameOptionCount}
                  onChange={e => updateField('gameOptionCount', e.target.value)}
                  required
                />
                <div className="form-text">Number of definition options shown for each game question.</div>
              </div>

              <div className="mb-3">
                <label htmlFor="gameQuestionCount" className="form-label">
                  Game question count
                </label>
                <input
                  id="gameQuestionCount"
                  type="number"
                  min="1"
                  className="form-control"
                  value={settings.gameQuestionCount}
                  onChange={e => updateField('gameQuestionCount', e.target.value)}
                  required
                />
                <div className="form-text">Number of questions in each game session.</div>
              </div>

              <button type="submit" className="btn btn-success" disabled={saving}>
                {saving ? 'Saving...' : 'Save Settings'}
              </button>
            </div>
          </form>
        )}
      </div>
    </Layout>
  );
}
