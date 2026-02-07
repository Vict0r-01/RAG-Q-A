import React, { useEffect, useRef } from 'react';
import MessageBox from './MessageBox.jsx';

function Messages({ messages = [] }) {
    const containerRef = useRef(null);

    useEffect(() => {
        const el = containerRef.current;
        if (!el) return;
        // Smooth scroll to bottom when messages change
        el.scrollTo({ top: el.scrollHeight, behavior: 'smooth' });
    }, [messages]);

    return (
        <div ref={containerRef} className="flex flex-col rounded-lg bg-[#070707] space-y-2 overflow-y-auto [scrollbar-width:none] [&::-webkit-scrollbar]:hidden h-96 md:h-152 w-full">
            {messages.map((msg, index) => (
                <MessageBox key={index} message={msg} />
            ))}
        </div>
    );
}

export default React.memo(Messages, (prev, next) => {
    if (prev.messages.length !== next.messages.length) return false;
});