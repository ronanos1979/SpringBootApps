import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import Layout from '../../components/Layout';
import { createUser, listUsers, updateUser } from '../../api/client';

// JSP equivalent: user/addUser.jsp
export default function AddUser() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [form, setForm] = useState({
    username: '',
    password: '',
    email: '',
    firstName: '',
    lastName: '',
  });
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(Boolean(id));
  const [submitError, setSubmitError] = useState('');

  useEffect(() => {
    if (!id) {
      return;
    }

    listUsers()
      .then(allUsers => {
        const existing = allUsers.find(u => u.id === Number(id));

        if (!existing) {
          throw new Error('User not found.');
        }

        setForm({
          username: existing.username ?? '',
          password: '',
          email: existing.email ?? '',
          firstName: existing.firstName ?? '',
          lastName: existing.lastName ?? '',
        });
      })
      .catch(err => setSubmitError(err.message || 'Unable to load user.'))
      .finally(() => setLoading(false));
  }, [id]);

  // Single onChange handler for all fields using the input's name attribute
  function handleChange(e) {
    const { name, value } = e.target;
    setForm(prev => ({ ...prev, [name]: value }));
  }

  function validate() {
    const errs = {};
    if (!form.username.trim()) errs.username = 'Username is required';
    if (!id && !form.password.trim()) errs.password = 'Password is required';
    if (!form.email.trim()) errs.email = 'Email is required';
    if (!form.firstName.trim()) errs.firstName = 'First name is required';
    if (!form.lastName.trim()) errs.lastName = 'Last name is required';
    return errs;
  }

  async function handleSubmit(e) {
    e.preventDefault();
    const errs = validate();
    if (Object.keys(errs).length > 0) {
      setErrors(errs);
      return;
    }

    setSubmitError('');

    try {
      if (id) {
        await updateUser(id, form);
      } else {
        await createUser(form);
      }

      navigate('/users');
    } catch (err) {
      setSubmitError(err.message || 'Unable to save user.');
    }
  }

  return (
    <Layout>
      <div className="container">
        <h1>Enter User Details:</h1>
        {submitError && <div className="alert alert-danger">{submitError}</div>}
        {loading && <div className="alert alert-info">Loading user...</div>}
        <form onSubmit={handleSubmit}>
          <div className="mb-3">
            <label htmlFor="username" className="form-label">Username:</label>
            <input
              id="username"
              type="text"
              name="username"
              className="form-control"
              value={form.username}
              onChange={handleChange}
              required
            />
            {errors.username && <span className="text-warning">{errors.username}</span>}
          </div>
          <div className="mb-3">
            <label htmlFor="password" className="form-label">Password:</label>
            <input
              id="password"
              type="password"
              name="password"
              className="form-control"
              value={form.password}
              onChange={handleChange}
              required={!id}
            />
            {errors.password && <span className="text-warning">{errors.password}</span>}
          </div>
          <div className="mb-3">
            <label htmlFor="email" className="form-label">Email:</label>
            <input
              id="email"
              type="email"
              name="email"
              className="form-control"
              value={form.email}
              onChange={handleChange}
              required
            />
            {errors.email && <span className="text-warning">{errors.email}</span>}
          </div>
          <div className="mb-3">
            <label htmlFor="firstName" className="form-label">First Name:</label>
            <input
              id="firstName"
              type="text"
              name="firstName"
              className="form-control"
              value={form.firstName}
              onChange={handleChange}
              required
            />
            {errors.firstName && <span className="text-warning">{errors.firstName}</span>}
          </div>
          <div className="mb-3">
            <label htmlFor="lastName" className="form-label">Last Name:</label>
            <input
              id="lastName"
              type="text"
              name="lastName"
              className="form-control"
              value={form.lastName}
              onChange={handleChange}
              required
            />
            {errors.lastName && <span className="text-warning">{errors.lastName}</span>}
          </div>
          <button type="submit" className="btn btn-success" disabled={loading}>Submit</button>
        </form>
      </div>
    </Layout>
  );
}
