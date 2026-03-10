CREATE OR REPLACE FUNCTION public.find_in_set(str bigint, strlist text)
RETURNS boolean AS $$
BEGIN
    RETURN str::text = ANY(string_to_array(strlist, ','));
END;
$$ LANGUAGE plpgsql IMMUTABLE;

CREATE OR REPLACE FUNCTION public.find_in_set(str text, strlist text)
RETURNS boolean AS $$
BEGIN
    RETURN str = ANY(string_to_array(strlist, ','));
END;
$$ LANGUAGE plpgsql IMMUTABLE;