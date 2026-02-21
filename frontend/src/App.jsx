import { useEffect, useState, useContext, useRef } from 'react'
import './index.css'
import Messages from './components/Messages.jsx';
import Documents from './components/Documents.jsx';
import { SessionContext } from './context/SessionContext.jsx';
import { events, stream } from 'fetch-event-stream';
import loadingGif from './resources/loading.gif';
function App() {
  const {sessionId} = useContext(SessionContext);
  const [documents, setDocuments] = useState([]);
  const [loading, setLoading] = useState(false);
  const [request, setRequest] = useState(null);
  const [messages, setMessages] = useState([
    { text: "Hello! I'm VaikroRag, your AI assistant. How can I help you today?", isUser: false }
  ]);
  const fileInputRef =  useRef();
  const API_BASE = import.meta.env.VITE_BACKEND_URL || '';

  const addMessage = (text, isUser) => {
    setMessages(prevMessages => [...prevMessages, { text, isUser }]);
  }

  const updateLastMessage = (text) => {
  setMessages(prevMessages => {
    if (prevMessages.length === 0) return prevMessages;

    const updated = [...prevMessages];
    //Deep copy
    updated[updated.length - 1] = { 
      ...updated[updated.length - 1], 
      text: text 
    };

    return updated;
  });
}

  const addDocument = (id, title) => {
    setDocuments(prevDocs => [...prevDocs, {id, title} ]);
  }

  useEffect(() => {
    if(sessionId == null) return;

    getMessages();
    getDocuments();
  }, [sessionId]);

  //Get messages for session
  const getMessages = async () => {
    try {
      const response = await fetch(`${API_BASE}/getMessages/${sessionId}`, {
        method: 'GET',
        headers: {
          'Accept': 'application/json'
        }
      });
      if (response.ok) {
        const data = await response.json();

        const parsedMessages = [{ text: "Hello! I'm VaikroRag, your AI assistant. How can I help you today?", isUser: false }]
          .concat(data.map(msg => ({ text: msg.text, isUser: msg.isUser })));
        setMessages(parsedMessages);
      } else {
        const text = await response.text();
        console.error('Server error:', response.status, text);
      }
    } catch (error) {
      console.error('Error fetching messages:', error);
    }
  }

  //Get documents for session
  const getDocuments = async () => {
    try{
      const response = await fetch(`${API_BASE}/documents/retrieve/${sessionId}`, {
        method: 'GET',
        headers: {
          'Accept': 'application/json'
        }
      });
      if(response.ok){
        const data = await response.json();

        setDocuments(data.map(doc => ({ id: doc.id, title: doc.title })));
      }
      }catch (error) {
        console.error('Error fetching documents:', error);
      }
    }

    //Remove document by ID
  const removeDocument = async (docToRemove) => {

    try{
      const response = await fetch(`${API_BASE}/documents/delete/${sessionId}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        },
        body: JSON.stringify({ docId: docToRemove }),
      });
      if(!response.ok){
        const text = await response.text();
        console.error('Server error:', response.status, text);
        return;
      } 
    setDocuments(prev => prev.filter(d => d.id !== docToRemove));
    }catch (error) {
        console.error('Error:', error);
      }
}

//Upload document
  const submitDocument = async (file) => {
    try {
      const response = await fetch(`${API_BASE}/documents/upload/${sessionId}`, {
        method: 'POST',
        body: file
      });
      if (!response.ok) {
        const text = await response.text();
        console.error('Server error:', response.status, text);
        return;
      }
      // Try to parse JSON safely
      let data = null;
      try {
        data = await response.json();
        addDocument(data.id, data.title);
      } catch (e) {
        const text = await response.text();
        console.warn('Received non-JSON response:', text);
      }

    } catch (error) {
      console.error('Error:', error);
    }
  }

  //Handle file input change
  const onFileChange = async (e) => {
    const file = e.target.files[0];
    const formData = new FormData();
    if(file) {
      if(file.type !== 'application/pdf') {
        console.error('Only PDF files are supported.');
        return;
      }
      if(file.size > 50 * 1024 * 1024) {
        console.error('File size exceeds 50MB limit.');
        return;
      }
      setLoading(true);
      formData.append('file', file);
      await submitDocument(formData);
      setLoading(false);
    }
  }

  //Submit user prompt
  const submitPrompt = async (messagesBody) => {

    let abort = new AbortController();
    try{
      const response = await fetch(`${API_BASE}/raganswer/${sessionId}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'text/event-stream'         
        },
        signal: abort.signal,
        body: JSON.stringify(messagesBody),
      });
      if (response.ok) {
      addMessage('', false);
      let fullAnswer = "";
      
      // Use a ref to accumulate the answer
      const answerRef = { current: "" };
      
      // Throttle updates to every 50ms
      let lastUpdate = Date.now();
      const UPDATE_INTERVAL = 50;
      
      //Server-Sent Events response
      let stream = events(response, abort);
      for await (let event of stream) {
        answerRef.current += event.data + " ";
        fullAnswer = answerRef.current;
        
        const now = Date.now();
        if (now - lastUpdate >= UPDATE_INTERVAL) {
          updateLastMessage(fullAnswer);
          lastUpdate = now;
        }
      }
      
      // Final update to ensure we have the complete message
      updateLastMessage(fullAnswer);
      postMessage(fullAnswer, false);
    } else {
      const text = await response.text();
      console.error('Server error:', response.status, text);
    }
  } catch (error) {
    console.error('Error:', error);
  }
  setLoading(false);
}

  const postMessage = async (text, isUser) => {
    try {
      const m = { text, isUser, sessionId };
      const response = await fetch(`${API_BASE}/addMessage`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        },
        body: JSON.stringify(m),
      });
      if (!response.ok) {
        const respText = await response.text();
        console.error('Server error:', response.status, respText);
      }
    } catch (error) {
      console.error('Error posting message:', error);
    }
  }

  //Handle form submission
  const onSubmit = (e) => {
    if(request == null || request.trim() === '') return;
    e.preventDefault();
    const newMessages = [...messages, { text:request, isUser:true}];
    setMessages(newMessages);
    if(documents.length <= 0) {
      addMessage("Please add a PDF document to start the conversation.", false);
      
    } else {
    setLoading(true);
    postMessage(request, true);
    submitPrompt(newMessages);
    }
    // Clear the textarea after submitting
    setRequest('');
  }

  return (
    <div className='flex items-center justify-center w-full h-full'>
      <form className="relative flex flex-col items-stretch gap-6 md:w-3/5 w-full max-w-3xl glass border border-yellow-900/20 rounded-2xl p-6 shadow-2xl"
      onSubmit={onSubmit}>
        <header className="flex items-center justify-between">
          <div>
            <h1 className="text-4xl md:text-5xl font-extrabold tracking-tight text-yellow-300">VaikroRag</h1>
            <p className="mt-1 text-sm text-yellow-200/80">Conversational RAG assistant - Provide Document and Ask Questions. Does not support images.</p>
          </div>
          <div className="flex items-center">
            <div className="text-xs text-center text-yellow-100/80 bg-yellow-900/10 px-3 py-1 rounded-full border border-yellow-800">Model: GPT-5-mini</div>
          </div>
        </header>

        <div className="flex flex-col gap-3">
            <Messages messages={messages} />

          <div className="flex flex-row items-start md:items-end gap-3">
            <textarea
              className="flex-1 min-h-[40px] max-h-80 resize-none bg-transparent border border-yellow-900/30 rounded-xl p-3 text-yellow-50 placeholder-yellow-300 focus:outline-none focus:ring-2 focus:ring-yellow-400/40"
              placeholder="Enter your prompt here..."
              disabled={loading === true}
              value={request || ''}
              onChange={(e) => setRequest(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === 'Enter' && !e.shiftKey) {
                  e.preventDefault();
                  onSubmit(e);
                }
              }}
            ></textarea>

            <div className="flex flex-col gap-2 items-stretch md:items-end">
              <button
                type="submit"
                className="bg-gradient-to-r from-yellow-400 to-yellow-300 text-black font-semibold px-3 md:px-5 py-1 md:py-3 rounded-full shadow hover:scale-[1.05] transition-transform disabled:opacity-50 disabled:cursor-not-allowed"
                disabled={loading === true || request == null || request.trim() === ''}
              >
                <svg
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 24 24"
              width="20"
              height="20"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
              aria-hidden="true"
              className="inline-block align-middle mr-1"
            >
              {/* main plane body */}
              <path d="M22 2 L15 22 L11 13 L2 9 L22 2 Z" />
              {/* line detail (fold/wing) */}
              <path d="M22 2 L11 13" />
            </svg>
              </button>

              <button className={`cursor-pointer hover:scale-[1.05] text-[10px] md:text-sm text-yellow-100 bg-yellow-900/10 px-3 py-2 rounded-full border border-yellow-800 hover:bg-yellow-800/10 ${loading == true ? 'opacity-50 cursor-not-allowed' : ''}`}
                onClick={() => fileInputRef.current?.click()}
                type='button'>
                Upload PDF
                <input
                  ref={fileInputRef}
                  className='hidden'
                  id='uploadFile'
                  type="file"
                  onChange={onFileChange}
                  disabled={loading === true}
                />
              </button>
            </div>
          </div>

          {!loading ?
            <Documents documents={documents} onRemove={removeDocument} />
          : <img src={loadingGif} 
            alt='Loading Animation'
            className="h-15 mx-auto" />}
        </div>
      </form>
    </div>
  )
}

export default App;
