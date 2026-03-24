import React from 'react';
export {AuthProvider, useAuth} from "./AuthContext";
export type {User} from "./AuthContext";
export { Navbar } from './Navbar';

export const Button = ({ children }: { children: React.ReactNode }) => {
  return <button style={{ padding: '10px', background: 'blue', color: 'white' }}>{children}</button>;
};