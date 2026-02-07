'use client';
import { createContext, useState, useEffect, use } from 'react';

export const SessionContext = createContext();

export const SessionProvider = ({ children }) => {
    const [sessionId, setSessionId] = useState(null);
    useEffect(() => {
        // Generate a unique session ID on mount
        const generateSessionId = () => {
            return 'session-' + Math.random().toString(36).slice(2,9) + Date.now().toString(36);
        };
        if(sessionStorage.getItem('sessionId') != null){
            setSessionId(sessionStorage.getItem('sessionId'));
            return;
        }
        const id = generateSessionId();
        setSessionId(id);
        sessionStorage.setItem('sessionId', id);
    }, []);
    return (
        <SessionContext.Provider value={{ sessionId, setSessionId }}>
            {children}
        </SessionContext.Provider>
    );
}

