import md5 from 'blueimp-md5';
import {jwtDecode, JwtPayload} from 'jwt-decode';

export function gravatarUrl(email: string): string {
  const emailHash = md5(email.trim().toLowerCase());
  return `https://www.gravatar.com/avatar/${emailHash}?s=150&d=identicon`;
}

export function getUserId(): string {
  const token = localStorage.getItem('token') || '';
  const decoded = jwtDecode<JwtPayload>(token);
  return <string>decoded.sub;
}

export function formatDate(date: string | Date): string {
  const now = new Date();
  let dateToFormat: Date;
  
  if (typeof date === 'string') {
    // Try to parse the date string
    if (date.includes('T')) {
      // ISO format
      dateToFormat = new Date(date);
    } else {
      // Try to parse as timestamp
      const timestamp = parseInt(date);
      if (!isNaN(timestamp)) {
        dateToFormat = new Date(timestamp);
      } else {
        // Fallback to current date if parsing fails
        dateToFormat = new Date();
      }
    }
  } else {
    dateToFormat = date;
  }
  
  if (isNaN(dateToFormat.getTime())) {
    return 'now';
  }

  const diff = now.getTime() - dateToFormat.getTime();
  const seconds = Math.floor(diff / 1000);
  const minutes = Math.floor(seconds / 60);
  const hours = Math.floor(minutes / 60);
  const days = Math.floor(hours / 24);

  if (seconds < 30) {
    return 'now';
  } else if (seconds < 60) {
    return 'just now';
  } else if (minutes < 60) {
    return `${minutes}m ago`;
  } else if (hours < 24) {
    return `${hours}h ago`;
  } else {
    return `${days}d ago`;
  }
}
