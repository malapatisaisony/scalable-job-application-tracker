import { useEffect, useState } from 'react';
import { authenticatedFetch } from './api';
import './index.css';

function App() {
  const [authMode, setAuthMode] = useState('login');

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(false);

  const [loggedIn, setLoggedIn] = useState(
      Boolean(localStorage.getItem('accessToken'))
  );

  const [jobs, setJobs] = useState([]);
  const [jobsLoading, setJobsLoading] = useState(false);
  const [jobsError, setJobsError] = useState('');

  const [showAddForm, setShowAddForm] = useState(false);
  const [editingJobId, setEditingJobId] = useState(null);

  const [companyName, setCompanyName] = useState('');
  const [jobTitle, setJobTitle] = useState('');
  const [jobUrl, setJobUrl] = useState('');
  const [location, setLocation] = useState('');
  const [status, setStatus] = useState('SAVED');

  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('');

  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  useEffect(() => {
    if (loggedIn) {
      loadJobs('', '', 0);
    }
  }, [loggedIn]);

  async function handleLogin(event) {
    event.preventDefault();

    setLoading(true);
    setMessage('');

    try {
      const response = await fetch(
          'http://localhost:8080/api/users/login',
          {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
            },
            body: JSON.stringify({
              email,
              password,
            }),
          }
      );

      if (!response.ok) {
        setMessage('Invalid email or password.');
        return;
      }

      const data = await response.json();

      localStorage.setItem(
          'accessToken',
          data.accessToken
      );

      localStorage.setItem(
          'refreshToken',
          data.refreshToken
      );

      setLoggedIn(true);

    } catch (error) {
      setMessage(
          'Could not connect to the backend.'
      );
    } finally {
      setLoading(false);
    }
  }

  async function handleRegister(event) {
    event.preventDefault();

    setMessage('');

    if (password !== confirmPassword) {
      setMessage('Passwords do not match.');
      return;
    }

    if (password.length < 8) {
      setMessage(
          'Password must be at least 8 characters.'
      );
      return;
    }

    setLoading(true);

    try {
      const response = await fetch(
          'http://localhost:8080/api/users/register',
          {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
            },
            body: JSON.stringify({
              email,
              password,
            }),
          }
      );

      if (!response.ok) {
        if (response.status === 409) {
          setMessage(
              'An account with this email already exists.'
          );
        } else {
          setMessage(
              'Could not create the account.'
          );
        }

        return;
      }

      setPassword('');
      setConfirmPassword('');

      setAuthMode('login');

      setMessage(
          'Account created successfully. Please sign in.'
      );

    } catch (error) {
      setMessage(
          'Could not connect to the backend.'
      );
    } finally {
      setLoading(false);
    }
  }

  function handleSessionExpired() {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');

    setLoggedIn(false);
    setJobs([]);

    setEmail('');
    setPassword('');
    setConfirmPassword('');

    setJobsError('');
    setShowAddForm(false);

    setSearch('');
    setStatusFilter('');

    setPage(0);
    setTotalPages(0);

    resetApplicationForm();

    setAuthMode('login');

    setMessage(
        'Your session expired. Please sign in again.'
    );
  }

  async function loadJobs(
      searchValue = search,
      statusValue = statusFilter,
      pageValue = page
  ) {
    setJobsLoading(true);
    setJobsError('');

    const params = new URLSearchParams();

    params.set('page', pageValue);
    params.set('size', '10');

    if (searchValue.trim()) {
      params.set(
          'search',
          searchValue.trim()
      );
    }

    if (statusValue) {
      params.set(
          'status',
          statusValue
      );
    }

    try {
      const response =
          await authenticatedFetch(
              `/api/job-applications?${params.toString()}`
          );

      if (response.status === 401) {
        handleSessionExpired();
        return;
      }

      if (!response.ok) {
        setJobsError(
            'Could not load job applications.'
        );
        return;
      }

      const data = await response.json();

      setJobs(data.content);
      setPage(data.number);
      setTotalPages(data.totalPages);

    } catch (error) {
      setJobsError(
          'Could not connect to the backend.'
      );
    } finally {
      setJobsLoading(false);
    }
  }

  function handleSearch(event) {
    event.preventDefault();

    setPage(0);

    loadJobs(
        search,
        statusFilter,
        0
    );
  }

  function handleClearFilters() {
    setSearch('');
    setStatusFilter('');
    setPage(0);

    loadJobs('', '', 0);
  }

  function handlePreviousPage() {
    if (page > 0) {
      loadJobs(
          search,
          statusFilter,
          page - 1
      );
    }
  }

  function handleNextPage() {
    if (page + 1 < totalPages) {
      loadJobs(
          search,
          statusFilter,
          page + 1
      );
    }
  }

  function resetApplicationForm() {
    setCompanyName('');
    setJobTitle('');
    setJobUrl('');
    setLocation('');
    setStatus('SAVED');
    setEditingJobId(null);
  }

  function handleOpenAddForm() {
    resetApplicationForm();
    setJobsError('');
    setShowAddForm(true);
  }

  function handleCloseForm() {
    resetApplicationForm();
    setShowAddForm(false);
  }

  function handleEditApplication(job) {
    setCompanyName(job.companyName);
    setJobTitle(job.jobTitle);
    setJobUrl(job.jobUrl || '');
    setLocation(job.location || '');
    setStatus(job.status);

    setEditingJobId(job.id);
    setJobsError('');
    setShowAddForm(true);

    window.scrollTo({
      top: 0,
      behavior: 'smooth',
    });
  }

  async function handleSaveApplication() {
    if (
        !companyName.trim() ||
        !jobTitle.trim()
    ) {
      setJobsError(
          'Company name and job title are required.'
      );
      return;
    }

    setJobsError('');

    const isEditing =
        editingJobId !== null;

    const path = isEditing
        ? `/api/job-applications/${editingJobId}`
        : '/api/job-applications';

    const method = isEditing
        ? 'PATCH'
        : 'POST';

    try {
      const response =
          await authenticatedFetch(
              path,
              {
                method,

                headers: {
                  'Content-Type':
                      'application/json',
                },

                body: JSON.stringify({
                  companyName,
                  jobTitle,
                  jobUrl,
                  location,
                  status,
                }),
              }
          );

      if (response.status === 401) {
        handleSessionExpired();
        return;
      }

      if (!response.ok) {
        setJobsError(
            isEditing
                ? 'Could not update the application.'
                : 'Could not save the application.'
        );

        return;
      }

      resetApplicationForm();
      setShowAddForm(false);

      if (isEditing) {
        await loadJobs(
            search,
            statusFilter,
            page
        );
      } else {
        setPage(0);

        await loadJobs(
            search,
            statusFilter,
            0
        );
      }

    } catch (error) {
      setJobsError(
          'Could not connect to the backend.'
      );
    }
  }

  async function handleDeleteApplication(id) {
    const confirmed = window.confirm(
        'Are you sure you want to delete this application?'
    );

    if (!confirmed) {
      return;
    }

    try {
      const response =
          await authenticatedFetch(
              `/api/job-applications/${id}`,
              {
                method: 'DELETE',
              }
          );

      if (response.status === 401) {
        handleSessionExpired();
        return;
      }

      if (!response.ok) {
        setJobsError(
            'Could not delete the application.'
        );
        return;
      }

      const targetPage =
          jobs.length === 1 && page > 0
              ? page - 1
              : page;

      await loadJobs(
          search,
          statusFilter,
          targetPage
      );

    } catch (error) {
      setJobsError(
          'Could not connect to the backend.'
      );
    }
  }

  async function handleLogout() {
    const refreshToken =
        localStorage.getItem('refreshToken');

    try {
      if (refreshToken) {
        await fetch(
            'http://localhost:8080/api/users/logout',
            {
              method: 'POST',

              headers: {
                'Content-Type':
                    'application/json',
              },

              body: JSON.stringify({
                refreshToken,
              }),
            }
        );
      }

    } catch (error) {
      console.error(
          'Logout request failed:',
          error
      );

    } finally {
      localStorage.removeItem(
          'accessToken'
      );

      localStorage.removeItem(
          'refreshToken'
      );

      setLoggedIn(false);
      setJobs([]);

      setEmail('');
      setPassword('');
      setConfirmPassword('');

      setMessage('');
      setJobsError('');

      setShowAddForm(false);

      setSearch('');
      setStatusFilter('');

      setPage(0);
      setTotalPages(0);

      resetApplicationForm();

      setAuthMode('login');
    }
  }

  if (loggedIn) {
    return (
        <div className="dashboard-page">

          <header className="dashboard-header">

            <div className="brand">

              <div className="brand-icon">
                JT
              </div>

              <h1>
                JobTracker
              </h1>

            </div>

            <button
                className="logout-button"
                onClick={handleLogout}
            >
              Logout
            </button>

          </header>

          <main className="dashboard-content">

            <div className="dashboard-title-row">

              <div>

                <h2>
                  My Applications
                </h2>

                <p>
                  Track and manage your job applications.
                </p>

              </div>

              <button
                  className="add-button"
                  onClick={handleOpenAddForm}
              >
                + Add Application
              </button>

            </div>

            <form
                className="filter-bar"
                onSubmit={handleSearch}
            >

              <input
                  className="search-input"
                  type="text"
                  placeholder="Search company or job title..."
                  value={search}
                  onChange={(event) =>
                      setSearch(
                          event.target.value
                      )
                  }
              />

              <select
                  className="filter-select"
                  value={statusFilter}
                  onChange={(event) =>
                      setStatusFilter(
                          event.target.value
                      )
                  }
              >

                <option value="">
                  All Statuses
                </option>

                <option value="SAVED">
                  Saved
                </option>

                <option value="APPLIED">
                  Applied
                </option>

                <option value="ONLINE_ASSESSMENT">
                  Online Assessment
                </option>

                <option value="PHONE_SCREEN">
                  Phone Screen
                </option>

                <option value="INTERVIEW">
                  Interview
                </option>

                <option value="OFFER">
                  Offer
                </option>

                <option value="REJECTED">
                  Rejected
                </option>

                <option value="WITHDRAWN">
                  Withdrawn
                </option>

              </select>

              <button
                  className="search-button"
                  type="submit"
              >
                Search
              </button>

              <button
                  className="clear-button"
                  type="button"
                  onClick={handleClearFilters}
              >
                Clear
              </button>

            </form>

            {showAddForm && (
                <div className="add-form-card">

                  <div className="form-header">

                    <h3>
                      {editingJobId
                          ? 'Edit Application'
                          : 'Add Application'}
                    </h3>

                    <button
                        className="close-button"
                        onClick={handleCloseForm}
                    >
                      ✕
                    </button>

                  </div>

                  <div className="form-group">
                    <label>
                      Company Name
                    </label>

                    <input
                        value={companyName}
                        onChange={(event) =>
                            setCompanyName(
                                event.target.value
                            )
                        }
                        placeholder="Amazon"
                    />
                  </div>

                  <div className="form-group">
                    <label>
                      Job Title
                    </label>

                    <input
                        value={jobTitle}
                        onChange={(event) =>
                            setJobTitle(
                                event.target.value
                            )
                        }
                        placeholder="Software Engineer"
                    />
                  </div>

                  <div className="form-group">
                    <label>
                      Job URL
                    </label>

                    <input
                        value={jobUrl}
                        onChange={(event) =>
                            setJobUrl(
                                event.target.value
                            )
                        }
                        placeholder="https://..."
                    />
                  </div>

                  <div className="form-group">
                    <label>
                      Location
                    </label>

                    <input
                        value={location}
                        onChange={(event) =>
                            setLocation(
                                event.target.value
                            )
                        }
                        placeholder="New York"
                    />
                  </div>

                  <div className="form-group">
                    <label>
                      Status
                    </label>

                    <select
                        value={status}
                        onChange={(event) =>
                            setStatus(
                                event.target.value
                            )
                        }
                    >

                      <option value="SAVED">
                        Saved
                      </option>

                      <option value="APPLIED">
                        Applied
                      </option>

                      <option value="ONLINE_ASSESSMENT">
                        Online Assessment
                      </option>

                      <option value="PHONE_SCREEN">
                        Phone Screen
                      </option>

                      <option value="INTERVIEW">
                        Interview
                      </option>

                      <option value="OFFER">
                        Offer
                      </option>

                      <option value="REJECTED">
                        Rejected
                      </option>

                      <option value="WITHDRAWN">
                        Withdrawn
                      </option>

                    </select>
                  </div>

                  <button
                      className="login-button"
                      onClick={handleSaveApplication}
                  >
                    {editingJobId
                        ? 'Update Application'
                        : 'Save Application'}
                  </button>

                </div>
            )}

            {jobsLoading && (
                <p>
                  Loading applications...
                </p>
            )}

            {jobsError && (
                <p className="login-message">
                  {jobsError}
                </p>
            )}

            <div className="jobs-grid">

              {jobs.map((job) => (
                  <div
                      className="job-card"
                      key={job.id}
                  >

                    <div className="job-card-top">

                      <div>

                        <h3>
                          {job.companyName}
                        </h3>

                        <p className="job-title">
                          {job.jobTitle}
                        </p>

                      </div>

                      <span className="status-badge">
                    {job.status}
                  </span>

                    </div>

                    <p className="job-location">
                      {job.location ||
                          'Location not specified'}
                    </p>

                    <div className="job-actions">

                      <button
                          className="edit-button"
                          onClick={() =>
                              handleEditApplication(job)
                          }
                      >
                        Edit
                      </button>

                      <button
                          className="delete-button"
                          onClick={() =>
                              handleDeleteApplication(
                                  job.id
                              )
                          }
                      >
                        Delete
                      </button>

                    </div>

                  </div>
              ))}

            </div>

            {!jobsLoading &&
                !jobsError &&
                jobs.length === 0 && (
                    <p className="empty-message">
                      No matching applications found.
                    </p>
                )}

            {totalPages > 0 && (
                <div className="pagination">

                  <button
                      className="pagination-button"
                      onClick={handlePreviousPage}
                      disabled={page === 0}
                  >
                    Previous
                  </button>

                  <span className="page-number">
                Page {page + 1} of {totalPages}
              </span>

                  <button
                      className="pagination-button"
                      onClick={handleNextPage}
                      disabled={
                          page + 1 >= totalPages
                      }
                  >
                    Next
                  </button>

                </div>
            )}

          </main>

        </div>
    );
  }

  if (authMode === 'register') {
    return (
        <div className="page">

          <div className="login-card">

            <div className="brand">

              <div className="brand-icon">
                JT
              </div>

              <h1>
                JobTracker
              </h1>

            </div>

            <p className="subtitle">
              Create an account to start tracking
              your job applications.
            </p>

            <h2>
              Create account
            </h2>

            <p className="login-text">
              Enter your details below.
            </p>

            <form onSubmit={handleRegister}>

              <div className="form-group">

                <label>
                  Email
                </label>

                <input
                    type="email"
                    placeholder="you@example.com"
                    value={email}
                    onChange={(event) =>
                        setEmail(
                            event.target.value
                        )
                    }
                    required
                />

              </div>

              <div className="form-group">

                <label>
                  Password
                </label>

                <input
                    type="password"
                    placeholder="Minimum 8 characters"
                    value={password}
                    minLength="8"
                    maxLength="72"
                    onChange={(event) =>
                        setPassword(
                            event.target.value
                        )
                    }
                    required
                />

              </div>

              <div className="form-group">

                <label>
                  Confirm Password
                </label>

                <input
                    type="password"
                    placeholder="Enter password again"
                    value={confirmPassword}
                    minLength="8"
                    maxLength="72"
                    onChange={(event) =>
                        setConfirmPassword(
                            event.target.value
                        )
                    }
                    required
                />

              </div>

              <button
                  className="login-button"
                  type="submit"
                  disabled={loading}
              >
                {loading
                    ? 'Creating account...'
                    : 'Create account'}
              </button>

            </form>

            {message && (
                <p className="login-message">
                  {message}
                </p>
            )}

            <p className="register-text">
              Already have an account?{' '}

              <span
                  onClick={() => {
                    setAuthMode('login');
                    setMessage('');
                    setPassword('');
                    setConfirmPassword('');
                  }}
              >
              Sign in
            </span>
            </p>

          </div>

        </div>
    );
  }

  return (
      <div className="page">

        <div className="login-card">

          <div className="brand">

            <div className="brand-icon">
              JT
            </div>

            <h1>
              JobTracker
            </h1>

          </div>

          <p className="subtitle">
            Track applications, interviews,
            and offers in one place.
          </p>

          <h2>
            Welcome back
          </h2>

          <p className="login-text">
            Sign in to continue to your dashboard.
          </p>

          <form onSubmit={handleLogin}>

            <div className="form-group">

              <label>
                Email
              </label>

              <input
                  type="email"
                  placeholder="you@example.com"
                  value={email}
                  onChange={(event) =>
                      setEmail(
                          event.target.value
                      )
                  }
                  required
              />

            </div>

            <div className="form-group">

              <label>
                Password
              </label>

              <input
                  type="password"
                  placeholder="Enter your password"
                  value={password}
                  onChange={(event) =>
                      setPassword(
                          event.target.value
                      )
                  }
                  required
              />

            </div>

            <button
                className="login-button"
                type="submit"
                disabled={loading}
            >
              {loading
                  ? 'Signing in...'
                  : 'Sign in'}
            </button>

          </form>

          {message && (
              <p className="login-message">
                {message}
              </p>
          )}

          <p className="register-text">
            Don't have an account?{' '}

            <span
                onClick={() => {
                  setAuthMode('register');
                  setMessage('');
                  setPassword('');
                  setConfirmPassword('');
                }}
            >
            Create account
          </span>
          </p>

        </div>

      </div>
  );
}

export default App;