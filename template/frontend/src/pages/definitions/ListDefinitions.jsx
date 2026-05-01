import { useState } from 'react';
import Layout from '../../components/Layout';
import { definitions as initialDefinitions } from '../../data/mockData';

// JSP equivalent: definition/listDefinitions.jsp
export default function ListDefinitions() {
  const [definitions, setDefinitions] = useState(initialDefinitions);

  function handleDelete(id) {
    setDefinitions(prev => prev.filter(d => d.id !== id));
  }

  return (
    <Layout>
      <div className="container">
        <h1>The Definitions are:</h1>
        <table className="table">
          <thead>
            <tr>
              <th>Id</th>
              <th>Word</th>
              <th>Language</th>
              <th>Definition Text</th>
              <th>Part Of Speech</th>
              <th>Example</th>
              <th>Source API</th>
              <th>Cached At</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {definitions.map(def => (
              <tr key={def.id}>
                <td>{def.id}</td>
                <td>{def.word.text}</td>
                <td>{def.word.language}</td>
                <td>{def.definitionText}</td>
                <td>{def.partOfSpeech}</td>
                <td>{def.example}</td>
                <td>
                  <a href={def.sourceApi} target="_blank" rel="noreferrer">{def.sourceApi}</a>
                </td>
                <td>{def.cachedAt}</td>
                <td>
                  <button onClick={() => handleDelete(def.id)} className="btn btn-warning">
                    Delete
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </Layout>
  );
}
