import { useState } from 'react';
import Layout from '../../components/Layout';
import { getAdminSettings, getGameQuestion } from '../../api/client';

export default function Game() {
  const [mode, setMode] = useState('easy');
  const [question, setQuestion] = useState(null);
  const [selectedId, setSelectedId] = useState(null);
  const [totalQuestions, setTotalQuestions] = useState(10);
  const [questionNumber, setQuestionNumber] = useState(0);
  const [correctAnswers, setCorrectAnswers] = useState(0);
  const [completed, setCompleted] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  async function loadQuestion(nextMode = mode, nextQuestionNumber = questionNumber || 1) {
    setLoading(true);
    setError('');
    setSelectedId(null);
    try {
      const data = await getGameQuestion(nextMode);
      setQuestion(data);
      setQuestionNumber(nextQuestionNumber);
    } catch (err) {
      setQuestion(null);
      setError(err.message || 'Unable to load game question.');
    } finally {
      setLoading(false);
    }
  }

  async function startGame(nextMode = mode) {
    setLoading(true);
    setError('');
    setSelectedId(null);
    setCompleted(false);
    setQuestion(null);
    setQuestionNumber(0);
    setCorrectAnswers(0);
    try {
      const settings = await getAdminSettings();
      const count = Math.max(1, Number(settings.gameQuestionCount) || 10);
      setTotalQuestions(count);
      const data = await getGameQuestion(nextMode);
      setQuestion(data);
      setQuestionNumber(1);
    } catch (err) {
      setError(err.message || 'Unable to start game.');
    } finally {
      setLoading(false);
    }
  }

  function handleModeChange(nextMode) {
    setMode(nextMode);
    startGame(nextMode);
  }

  function handleAnswer(definitionId) {
    if (selectedId !== null) return;
    setSelectedId(definitionId);
    if (definitionId === question?.correctDefinitionId) {
      setCorrectAnswers(prev => prev + 1);
    }
  }

  function handleNext() {
    if (questionNumber >= totalQuestions) {
      setQuestion(null);
      setCompleted(true);
      return;
    }
    loadQuestion(mode, questionNumber + 1);
  }

  const answered = selectedId !== null;
  const correct = answered && selectedId === question?.correctDefinitionId;

  return (
    <Layout>
      <div className="container">
        <div className="d-flex justify-content-between align-items-center mb-3">
          <h1>Game</h1>
          <div className="btn-group" role="group" aria-label="Game mode">
            <button
              type="button"
              className={`btn ${mode === 'easy' ? 'btn-primary' : 'btn-outline-primary'}`}
              onClick={() => handleModeChange('easy')}
              disabled={loading}
            >
              Easy
            </button>
            <button
              type="button"
              className={`btn ${mode === 'difficult' ? 'btn-primary' : 'btn-outline-primary'}`}
              onClick={() => handleModeChange('difficult')}
              disabled={loading}
            >
              Difficult
            </button>
          </div>
        </div>

        {error && <div className="alert alert-danger">{error}</div>}

        {!question && !loading && !error && (
          <>
            {completed && (
              <div className="card mb-3">
                <div className="card-body">
                  <h2 className="h4">Results</h2>
                  <div className="display-6">{correctAnswers}/{totalQuestions}</div>
                </div>
              </div>
            )}
            <button type="button" className="btn btn-success" onClick={() => startGame()}>
              {completed ? 'Play Again' : 'Start Game'}
            </button>
          </>
        )}

        {loading && <div className="alert alert-info">Loading question...</div>}

        {question && !loading && (
          <>
            <div className="mb-3">
              <div className="d-flex justify-content-between align-items-center mb-2">
                <div className="text-muted text-uppercase small">{question.mode}</div>
                <span className="badge bg-dark">{questionNumber}/{totalQuestions}</span>
              </div>
              <div className="display-6">{question.wordText}</div>
              <span className="badge bg-secondary">{question.language}</span>
            </div>

            <div className="list-group mb-3">
              {question.options.map(option => {
                const isSelected = selectedId === option.definitionId;
                const isCorrect = answered && option.definitionId === question.correctDefinitionId;
                const className = [
                  'list-group-item',
                  'list-group-item-action',
                  'border',
                  isCorrect ? 'list-group-item-success border-success border-3 fw-semibold' : '',
                  isSelected && !isCorrect ? 'list-group-item-danger border-danger border-3 fw-semibold' : '',
                ].filter(Boolean).join(' ');

                return (
                  <button
                    key={option.definitionId}
                    type="button"
                    className={className}
                    onClick={() => handleAnswer(option.definitionId)}
                    disabled={answered}
                  >
                    <span className="d-flex align-items-start gap-2">
                      <span className="d-inline-flex align-items-center justify-content-center" style={{ width: '1.5rem' }}>
                        {isCorrect && <i className="bi bi-check-circle-fill text-success" aria-label="Correct">✓</i>}
                        {isSelected && !isCorrect && <i className="bi bi-x-circle-fill text-danger" aria-label="Incorrect">✕</i>}
                      </span>
                      <span>
                        <span className="badge bg-info text-dark me-2">{option.partOfSpeech}</span>
                        {option.definitionText}
                      </span>
                    </span>
                  </button>
                );
              })}
            </div>

            {answered && (
              <div className={`alert ${correct ? 'alert-success' : 'alert-warning'}`}>
                {correct ? 'Correct.' : 'Not quite.'}
              </div>
            )}

            <button type="button" className="btn btn-success" onClick={handleNext} disabled={loading || !answered}>
              {questionNumber >= totalQuestions ? 'Show Results' : 'Next Word'}
            </button>
          </>
        )}
      </div>
    </Layout>
  );
}
