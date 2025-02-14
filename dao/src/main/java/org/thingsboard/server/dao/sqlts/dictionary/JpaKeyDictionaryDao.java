/**
 * Copyright © 2016-2024 The Winstarcloud Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.winstarcloud.server.dao.sqlts.dictionary;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.winstarcloud.server.dao.dictionary.KeyDictionaryDao;
import org.winstarcloud.server.dao.model.sqlts.dictionary.KeyDictionaryCompositeKey;
import org.winstarcloud.server.dao.model.sqlts.dictionary.KeyDictionaryEntry;
import org.winstarcloud.server.dao.sql.JpaAbstractDaoListeningExecutorService;
import org.winstarcloud.server.dao.util.SqlDao;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

@Component
@Slf4j
@SqlDao
public class JpaKeyDictionaryDao extends JpaAbstractDaoListeningExecutorService implements KeyDictionaryDao {

    private final ConcurrentMap<String, Integer> keyDictionaryMap = new ConcurrentHashMap<>();
    protected static final ReentrantLock creationLock = new ReentrantLock();

    @Autowired
    private KeyDictionaryRepository keyDictionaryRepository;

    @Override
    public Integer getOrSaveKeyId(String strKey) {
        Integer keyId = keyDictionaryMap.get(strKey);
        if (keyId == null) {
            Optional<KeyDictionaryEntry> tsKvDictionaryOptional;
            tsKvDictionaryOptional = keyDictionaryRepository.findById(new KeyDictionaryCompositeKey(strKey));
            if (tsKvDictionaryOptional.isEmpty()) {
                creationLock.lock();
                try {
                    keyId = keyDictionaryMap.get(strKey);
                    if (keyId != null) {
                        return keyId;
                    }
                    tsKvDictionaryOptional = keyDictionaryRepository.findById(new KeyDictionaryCompositeKey(strKey));
                    if (tsKvDictionaryOptional.isEmpty()) {
                        KeyDictionaryEntry keyDictionaryEntry = new KeyDictionaryEntry();
                        keyDictionaryEntry.setKey(strKey);
                        try {
                            KeyDictionaryEntry saved = keyDictionaryRepository.save(keyDictionaryEntry);
                            keyDictionaryMap.put(saved.getKey(), saved.getKeyId());
                            keyId = saved.getKeyId();
                        } catch (DataIntegrityViolationException | ConstraintViolationException e) {
                            tsKvDictionaryOptional = keyDictionaryRepository.findById(new KeyDictionaryCompositeKey(strKey));
                            KeyDictionaryEntry dictionary = tsKvDictionaryOptional.orElseThrow(() -> new RuntimeException("Failed to get KeyDictionaryEntry entity from DB!"));
                            keyDictionaryMap.put(dictionary.getKey(), dictionary.getKeyId());
                            keyId = dictionary.getKeyId();
                        }
                    } else {
                        keyId = tsKvDictionaryOptional.get().getKeyId();
                    }
                } finally {
                    creationLock.unlock();
                }
            } else {
                keyId = tsKvDictionaryOptional.get().getKeyId();
                keyDictionaryMap.put(strKey, keyId);
            }
        }
        return keyId;
    }

    @Override
    public String getKey(Integer keyId) {
        Optional<KeyDictionaryEntry> byKeyId = keyDictionaryRepository.findByKeyId(keyId);
        return byKeyId.map(KeyDictionaryEntry::getKey).orElse(null);
    }

}
