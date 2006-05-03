/*
 * -----------------------------------------------------------------------------
 *
 * <p><b>License and Copyright: </b>The contents of this file are subject to the
 * Mozilla Public License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License
 * at <a href="http://www.mozilla.org/MPL">http://www.mozilla.org/MPL/.</a></p>
 *
 * <p>Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.</p>
 *
 * <p>The entire file consists of original code.  Copyright &copy; 2003, 2004
 * Tufts University. All rights reserved.</p>
 *
 * -----------------------------------------------------------------------------
 */

package edu.tufts.osidimpl.repository.favorites;

public class RepositoryIterator
implements org.osid.repository.RepositoryIterator
{
    private java.util.Iterator iterator = null;

    public RepositoryIterator(java.util.Vector vector)
    throws org.osid.repository.RepositoryException
    {
        this.iterator = vector.iterator();
    }

    public boolean hasNextRepository()
    throws org.osid.repository.RepositoryException
    {
        return (this.iterator.hasNext());
    }

    public org.osid.repository.Repository nextRepository()
    throws org.osid.repository.RepositoryException
    {
		try {
			return (org.osid.repository.Repository)iterator.next();
		} catch (Throwable t) {
            throw new org.osid.repository.RepositoryException(org.osid.shared.SharedException.NO_MORE_ITERATOR_ELEMENTS);
		}
    }
}
